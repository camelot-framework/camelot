package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.api.PluginsInterop;
import ru.yandex.qatools.camelot.common.*;
import ru.yandex.qatools.camelot.common.builders.*;
import ru.yandex.qatools.camelot.config.*;
import ru.yandex.qatools.camelot.error.MetadataException;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.String.format;
import static jodd.util.StringUtil.isEmpty;
import static ru.yandex.qatools.camelot.core.util.IOUtils.readResource;
import static ru.yandex.qatools.camelot.core.util.ServiceUtil.*;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.NameUtil.defaultPluginId;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class GenericPluginsEngine implements PluginsService, ReloadableService, RoutingService {
    public static final String PROPS_PATH = "classpath*:/camelot-default.properties";
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final CamelContext camelContext;
    protected final PluginLoader pluginLoader;
    protected final Resource[] configResources;
    protected final String inputUri;
    protected final String outputUri;
    protected List<PluginsConfig> pluginsConfigs;
    protected PluginsInterop interop;
    protected PluginTree pluginTree;
    protected Map<String, Plugin> pluginsMap;
    protected Map<String, Plugin> pluginsByClassMap;
    private PluginContextInjector contextInjector;
    private BuildersFactory buildersFactory;
    private volatile boolean loading = false;
    private ResourceBuilder resourceBuilder;
    private AppConfig appConfig;
    private String engineName;
    private EventProducer mainInput;
    private PluginUriBuilder uriBuilder;
    private MessagesSerializer messagesSerializer;
    private InterimProcessor interimProcessor = null;

    public GenericPluginsEngine(Resource[] configResources, PluginLoader pluginLoader, CamelContext camelContext,
                                String inputUri, String outputUri) {
        this.configResources = configResources;
        this.camelContext = camelContext;
        this.pluginLoader = pluginLoader;
        this.inputUri = inputUri;
        this.outputUri = outputUri;
        this.engineName = getClass().getSimpleName();
        this.uriBuilder = new BasicPluginUriBuilder();
        this.messagesSerializer = new BasicMessagesSerializer();
        setBuildersFactory(new BuildersFactoryImpl());
        setContextInjector(new PluginContextInjectorImpl());
        setAppConfig(new AppConfigSystemProperties());
    }


    /**
     * Initialize the common configuration
     */
    @Override
    public synchronized void init() {
        startLoading();
        // set the interop ability
        this.interop = new PluginsInteropService(this);

        try {
            this.mainInput = initEventProducer(camelContext, inputUri, messagesSerializer);
            initializePlugins();
            initWebResources();
        } catch (Exception e) {
            logger.error("Could not initialize plugins configurations: {}", formatStackTrace(e), e);
        }
        stopLoading();
    }

    /**
     * Reinitialize the service with stopping the plugins first
     */
    @Override
    public void reload() {
        reloadAndStart();
    }

    /**
     * Reinitialize the service
     */
    @Override
    public synchronized void reloadAndStart() {
        startLoading();
        try {
            // empty the configuration to reload
            pluginsConfigs = null;
            pluginsMap = null;
            pluginsByClassMap = null;
            pluginTree = null;

            initializePlugins();
            initWebResources();

            camelContext.start();
        } catch (Exception e) {
            logger.error("Could not initialize plugins configurations", e);
        }
        stopLoading();
    }

    @Override
    public synchronized void stop() {
        try {
            stopPlugins();
        } catch (Exception e) {
            logger.error("Failed to stop plugins!", e);
        }
    }

    /**
     * Returns or initializes the plugin system configuration
     */
    public List<PluginsConfig> getConfigs() {
        if (pluginsConfigs == null) {
            try {
                pluginsConfigs = loadConfigs();
            } catch (Exception e) {
                logger.error("Could not load the configuration: ", e);
                throw new PluginsSystemException(e);
            }
        }
        return pluginsConfigs;
    }

    /**
     * Returns the plugin context by id
     */
    @Override
    public PluginContext getPluginContext(final String pluginId) {
        return getPlugin(pluginId).getContext();
    }

    /**
     * Returns the plugin context by class
     */
    @Override
    public PluginContext getPluginContext(Class pluginClass) {
        return getPlugin(pluginClass).getContext();
    }

    /**
     * Returns the configured plugin by class
     */
    @Override
    public Plugin getPlugin(final Class pluginClass) {
        final String name = (pluginClass != null) ? pluginClass.getName() : null;
        final Plugin plugin = getPluginsByClassMap().get(name);
        if (plugin != null) {
            return plugin;
        }
        throw new PluginsSystemException("Could not find the plugin with class " + pluginClass);
    }

    /**
     * Returns the configured plugin by id
     */
    @Override
    public Plugin getPlugin(final String pluginId) {
        final Plugin plugin = getPluginsMap().get(pluginId);
        if (plugin != null) {
            return plugin;
        }
        throw new PluginsSystemException("Could not find the plugin with name " + pluginId);
    }

    /**
     * Returns the built plugins tree
     */
    @Override
    public PluginTree getPluginTree() {
        if (pluginTree == null) {
            pluginTree = PluginTree.buildTree(getPluginsMap());
        }
        return pluginTree;
    }

    /**
     * Returns the map of the loaded plugins. Key - pluginId.
     */
    @Override
    public Map<String, Plugin> getPluginsByClassMap() {
        if (pluginsByClassMap == null) {
            try {
                pluginsByClassMap = buildPluginsByClassMap(getConfigs());
            } catch (ClassNotFoundException e) {
                throw new PluginsSystemException("Could not load class", e);
            }
        }
        return pluginsByClassMap;
    }

    /**
     * Returns the map of the loaded plugins. Key - pluginId.
     */
    @Override
    public Map<String, Plugin> getPluginsMap() {
        if (pluginsMap == null) {
            pluginsMap = buildPluginsMap(getConfigs());
        }
        return pluginsMap;
    }

    @Override
    public PluginLoader getLoader() {
        return pluginLoader;
    }


    @Override
    public String getInputUri() {
        return inputUri;
    }

    @Override
    public String getOutputUri() {
        return outputUri;
    }

    @Override
    public BuildersFactory getBuildersFactory() {
        return buildersFactory;
    }

    @Override
    public void setBuildersFactory(BuildersFactory buildersFactory) {
        this.buildersFactory = buildersFactory;
    }

    @Override
    public PluginUriBuilder getUriBuilder() {
        return uriBuilder;
    }

    @Override
    public void setUriBuilder(PluginUriBuilder uriBuilder) {
        this.uriBuilder = uriBuilder;
    }

    @Override
    public ResourceBuilder getResourceBuilder() {
        return resourceBuilder;
    }

    @Override
    public void setResourceBuilder(ResourceBuilder resourceBuilder) {
        this.resourceBuilder = resourceBuilder;
    }

    @Override
    public PluginContextInjector getContextInjector() {
        return contextInjector;
    }

    @Override
    public void setContextInjector(PluginContextInjector contextInjector) {
        this.contextInjector = contextInjector;
    }

    @Override
    public AppConfig getAppConfig() {
        return appConfig;
    }

    @Override
    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public PluginsInterop getInterop() {
        return interop;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    @Override
    public EventProducer getMainInput() {
        return mainInput;
    }

    @Override
    public MessagesSerializer getMessagesSerializer() {
        return messagesSerializer;
    }

    @Override
    public void setMessagesSerializer(MessagesSerializer messagesSerializer) {
        this.messagesSerializer = messagesSerializer;
    }

    public InterimProcessor getInterimProcessor() {
        return interimProcessor;
    }

    public void setInterimProcessor(InterimProcessor interimProcessor) {
        this.interimProcessor =
                (interimProcessor == null || interimProcessor.getClass().equals(DefaultInterimProcessor.class))
                        ? null : interimProcessor;
    }

    /**
     * Returns true if plugin contains aggregator or processor
     *
     * @param plugin
     */
    @Override
    public boolean pluginCanConsume(Plugin plugin) {
        return !isEmpty(plugin.getAggregator()) || !isEmpty(plugin.getProcessor());
    }

    /**
     * Is currently plugins are reloading
     */
    @Override
    public boolean isLoading() {
        return loading;
    }

    /** ------------------------------------------------------------- **/

    /**
     * Initializes the rest resources & client notify routes
     */
    protected void initWebResources() {
        try {
            if (getResourceBuilder() != null) {
                for (PluginsConfig config : getConfigs()) {
                    buildResources(config);
                }
            } else {
                logger.info("Skipping web resources initialization: there is no resource builder specified!");
            }
        } catch (Exception e) {
            logger.error("Could not register service beans from plugins", e);
        }
    }

    /**
     * Stop the plugins and their contexts
     */
    protected synchronized void stopPlugins() throws Exception { //NOSONAR
        for (PluginsConfig config : getConfigs()) {
            releasePluginsContexts(config);
        }
    }

    /**
     * Initialize the plugins and their contexts
     */
    protected synchronized void initializePlugins() throws Exception { //NOSONAR
        for (PluginsConfig config : getConfigs()) {
            initPluginsContexts(config);
        }
        for (Plugin plugin : getPluginsMap().values()) {
            initBasicPluginRoutes(plugin);
        }
    }

    /**
     * Stop the routes built upon this plugin
     */
    protected void stopRoutes(Plugin plugin) throws Exception { //NOSONAR
    }

    /**
     * Loads the plugin system configuration according to the setup
     */
    protected synchronized List<PluginsConfig> loadConfigs() throws IOException, ClassNotFoundException, JAXBException, URISyntaxException {
        if (configResources.length < 1) {
            throw new BeanInitializationException("Cannot initialize plugins system: config not found! Paths=" + Arrays.toString(configResources));
        }
        List<PluginsConfig> configs = new ArrayList<>();
        for (Resource resource : configResources) {
            JAXBContext jc = JAXBContext.newInstance(PluginsConfig.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            logger.info("Additional plugins configuration file: " + resource + "...");
            configs.add((PluginsConfig) unmarshaller.unmarshal(readResource(resource.getURL())));
        }
        return configs;
    }


    /**
     * Set the loading to true
     */
    protected void startLoading() {
        loading = true;
    }

    /**
     * Set the loading to false
     */
    protected void stopLoading() {
        loading = false;
    }

    /**
     * Initialize the basic routes for the plugin
     */
    protected void initBasicPluginRoutes(final Plugin plugin) throws Exception { //NOSONAR

    }

    protected  <T extends ProcessorDefinition> T addInterimRoute(T route) {
        if (getInterimProcessor() != null) {
            route.process(getInterimProcessor());
        }
        return route;
    }


    /**
     * Release the plugin configuration
     */
    protected void releasePluginsContexts(PluginsConfig config) throws Exception { //NOSONAR
        for (final PluginsSource source : config.getSources()) {
            try {
                pluginLoader.releaseClassLoader(source);
            } catch (Exception e) {
                logger.warn("Failed to release the classloader for the source" + source.getArtifact(), e);
            }
        }
    }

    /**
     * Initialize plugins configurations
     */
    protected void initPluginsContexts(PluginsConfig config) throws Exception { //NOSONAR
        for (final PluginsSource source : config.getSources()) {
            final ClassLoader classLoader = pluginLoader.createClassLoader(source);
            for (final Plugin plugin : source.getPlugins()) {
                initPluginContext(source, plugin, new PluginContext(), classLoader);
            }
            source.setAppConfig(initPluginAppConfig(getAppConfig(), classLoader, PROPS_PATH));
        }
    }

    /**
     * Initialize configuration for the plugin
     */
    protected void initPluginContext(PluginsSource source, final Plugin plugin,
                                     PluginContext context, ClassLoader classLoader) throws Exception { //NOSONAR
        context.setSource(source);
        plugin.setContext(context);
        if (pluginCanConsume(plugin)) {
            context.setPluginClass(isEmpty(plugin.getAggregator()) ? plugin.getProcessor() : plugin.getAggregator());
        } else {
            context.setPluginClass(plugin.getResource());
        }
        if (isEmpty(context.getPluginClass())) {
            throw new MetadataException(format("Plugin class cannot be empty for plugin %s!", plugin.getId()));
        }
        if (isEmpty(plugin.getId())) {
            plugin.setId(defaultPluginId(plugin));
        }
        if (isEmpty(plugin.getBaseInputUri())) {
            plugin.setBaseInputUri(uriBuilder.basePluginUri());
        }
        logger.info("Initializing plugin " + plugin.getId());
        final PluginEndpoints endpoints = new PluginEndpointsImpl(inputUri, plugin, getEngineName(), uriBuilder);
        final AggregationRepositoryBuilder repositoryBuilder = getBuildersFactory().newRepositoryBuilder(camelContext);
        context.setSource(source);
        context.setEndpoints(endpoints);
        context.setId(plugin.getId());
        context.setClassLoader(classLoader);
        context.setInterop(interop);
        context.setOutput(initEventProducer(camelContext, endpoints.getProducerUri(), messagesSerializer));
        context.setMainInput(mainInput);
        context.setClientSendersProvider(new ClientSendersProviderImpl(camelContext, endpoints.getFrontendSendUri(), messagesSerializer));
        context.setInjector(getContextInjector());
        context.setMessagesSerializer(messagesSerializer);
        context.setInterimProcessor(interimProcessor);
        if (pluginCanConsume(plugin)) {
            context.setInput(initEventProducer(camelContext, endpoints.getConsumerUri(), messagesSerializer));
            context.setStorage(repositoryBuilder.initStorage(plugin));
            context.setAggregationRepo(repositoryBuilder.initWritable(plugin));
            context.setRepository(repositoryBuilder.initReadonly(plugin));
        } else {
            logger.warn("Plugin {} does not contain processing code! It contains resource only", plugin.getId());
        }

        context.setAppConfig(initPluginAppConfig(getAppConfig(), classLoader, PROPS_PATH));
    }

    /** ------------------------------------------------------------- **/


    /**
     * Add all the registered resources to the context
     */
    private void buildResources(final PluginsConfig config) throws Exception { //NOSONAR
        for (final PluginsSource source : config.getSources()) {
            for (final Plugin plugin : source.getPlugins()) {
                getResourceBuilder().build(camelContext, plugin);
            }
        }
    }

    /**
     * Builds the map of the initialized plugins by class
     */
    private Map<String, Plugin> buildPluginsByClassMap(final List<PluginsConfig> configs) throws ClassNotFoundException {
        Map<String, Plugin> result = new HashMap<>();
        for (PluginsConfig config : configs) {
            for (final PluginsSource source : config.getSources()) {
                for (final Plugin plugin : source.getPlugins()) {
                    Class pluginClass = plugin.getContext().getClassLoader().loadClass(
                            plugin.getContext().getPluginClass()
                    );
                    result.put(pluginClass.getName(), plugin);
                }
            }
        }
        return result;
    }

    /**
     * Builds the map of the initialized plugins
     */
    private Map<String, Plugin> buildPluginsMap(final List<PluginsConfig> configs) {
        Map<String, Plugin> result = new HashMap<>();
        for (PluginsConfig config : configs) {
            for (final PluginsSource source : config.getSources()) {
                for (final Plugin plugin : source.getPlugins()) {
                    result.put(plugin.getId(), plugin);
                }
            }
        }
        return result;
    }


}
