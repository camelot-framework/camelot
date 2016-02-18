package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ShutdownPrepared;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.common.PluginInitializer;
import ru.yandex.qatools.camelot.common.PluginLoader;
import ru.yandex.qatools.camelot.common.ProcessingEngine;
import ru.yandex.qatools.camelot.common.builders.BasicRoutesBuilder;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializer;
import ru.yandex.qatools.camelot.common.builders.SchedulerBuildersFactory;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginsConfig;
import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.core.builders.NoSchedulerBuildersFactory;

import java.util.*;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.DEBUG;
import static ru.yandex.qatools.camelot.util.NameUtil.routeId;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveRoute;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ProcessingEngineImpl extends GenericPluginsEngine implements ProcessingEngine, ShutdownPrepared {
    public static final String CAMELOT_MULTICAST_PROFILE = "camelotMulticastProfile";

    private Scheduler scheduler;
    private SchedulerBuildersFactory schedulerBuildersFactory;
    private QuartzInitializer quartzInitializer;
    private PluginInitializer pluginInitializer;

    public ProcessingEngineImpl(Resource[] configResources, PluginLoader pluginLoader,
                                CamelContext camelContext, String inputUri, String outputUri) {
        super(configResources, pluginLoader, camelContext, inputUri, outputUri);
        pluginInitializer = new PluginInitializerImpl();
    }

    /**
     * Initialize the service
     */
    public synchronized void init() {
        super.init();

        this.schedulerBuildersFactory = (scheduler != null) ? getBuildersFactory().
                newSchedulerBuildersFactory(scheduler, camelContext) : new NoSchedulerBuildersFactory();
        try {
            for (PluginsConfig config : getConfigs()) {
                buildRoutes(config);
            }
        } catch (Exception e) {
            logger.error("Could not build routes from plugins", e);
        }

        try {
            buildConsumersRoutes();
        } catch (Exception e) {
            logger.error("Could not build consumers routes from plugins", e);
        }

        try {
            if (getScheduler() != null) {
                this.quartzInitializer = getQuartzFactory().newQuartzInitilizer(scheduler, getAppConfig());
                this.quartzInitializer.start();
            }
        } catch (Exception e) {
            logger.error("Could not start the scheduler", e);
        }
        try {
            camelContext.addStartupListener((context, alreadyStarted) -> { //NOSONAR
                for (Plugin plugin : getPluginsMap().values()) {
                    initPlugin(plugin);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to add camel context listener", e);
        }
        initialized = true;
    }

    @Override
    public void prepareShutdown(boolean suspendOnly, boolean forced) {
        for (Plugin plugin : getPluginsMap().values()) {
            try {
                unInitPlugin(plugin);
            } catch (Exception e) {
                logger.error(format("Failed to uninitialize the plugin %s", plugin.getId()), e);
            }
        }
        try {
            stopPlugins();
        } catch (Exception e) {
            logger.error("Failed to stop plugins!", e);
        }
        if (quartzInitializer != null) {
            quartzInitializer.stop();
        }
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public PluginInitializer getPluginInitializer() {
        return pluginInitializer;
    }

    @Override
    public void setPluginInitializer(PluginInitializer pluginInitializer) {
        this.pluginInitializer = pluginInitializer;
    }

    /**
     * ------------------------------------------------------------- *
     */

    /**
     * Builds all the necessary routes for the plugins
     */
    private void buildRoutes(final PluginsConfig config) throws Exception { //NOSONAR
        for (final PluginsSource source : config.getSources()) {
            for (final Plugin plugin : source.getPlugins()) {
                if (pluginCanConsume(plugin)) {
                    addPluginRoutes(plugin);
                }
            }
        }
    }

    /**
     * Builds all the consumers routes
     */
    private void buildConsumersRoutes() throws Exception { //NOSONAR
        final Map<String, Plugin> pluginsMap = getPluginsMap();
        final Map<String, Set<String>> consumersMap = getPluginsConsumersMap();
        // Add route to populate all the completed tests events to all the registered plugins or to stop
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception { //NOSONAR
                // Producer plugins messages go to the consumers from INPUT or other plugin
                for (String fromId : consumersMap.keySet()) {
                    final String fromUri = fromId == null ? inputUri
                            : pluginsMap.get(fromId).getContext().getEndpoints().getOutputUri();
                    String[] consumers = calcConsumers(consumersMap.get(fromId));
                    addInterimRoute(from(fromUri)
                            .multicast()
                            .executorServiceRef(CAMELOT_MULTICAST_PROFILE)
                            .parallelProcessing()
                            .log(DEBUG, format("===> ROUTE %s ===> [%s]", fromUri, StringUtils.join(consumers, ","))))
                            .to(consumers)
                            .routeId(routeId(fromUri, consumers));
                }
                // The rest of the plugins messages go to OUTPUT
                for (String fromId : pluginsMap.keySet()) {
                    final Plugin plugin = pluginsMap.get(fromId);
                    if (!consumersMap.keySet().contains(fromId) && pluginCanConsume(plugin)) {
                        final PluginEndpoints endpoints = plugin.getContext().getEndpoints();
                        addInterimRoute(from(endpoints.getOutputUri())
                                .routeId(endpoints.getOutputRouteId())
                                .multicast()
                                .executorServiceRef(CAMELOT_MULTICAST_PROFILE)
                                .parallelProcessing()
                                .log(DEBUG, format("===> ROUTE %s ===> %s", endpoints.getOutputUri(), outputUri)))
                                .to(new String[]{outputUri});
                    }
                }
            }
        });
    }

    /**
     * Calculate the consumers array from the set of consumers for the plugin (or null (as INPUT))
     */
    @SuppressWarnings("unchecked, SuspiciousMethodCalls")
    private String[] calcConsumers(Set<String> consumers) {
        final Collection consumersSet = CollectionUtils.collect(consumers, new Transformer() {
            @Override
            public Object transform(Object pluginId) {
                return getPluginsMap().get(pluginId).getContext().getEndpoints().getConsumerUri();
            }
        });
        return (String[]) (consumersSet).toArray(new String[consumersSet.size()]);
    }

    /**
     * Calc consumer plugins map for each plugin
     */
    private Map<String, Set<String>> getPluginsConsumersMap() {
        Map<String, Set<String>> result = new HashMap<>();
        for (final Plugin plugin : getPluginsMap().values()) {
            if (pluginCanConsume(plugin)) {
                String source = plugin.getSource();
                String from = source;
                if (source != null && !getPluginsMap().containsKey(source)) {
                    from = getPluginsByClassMap().get(source).getId();
                }
                final String id = plugin.getId();
                if (!result.containsKey(from)) {
                    result.put(from, new HashSet<String>());
                }
                result.get(from).add(id);
            }
        }
        return result;
    }

    /**
     * Adds the plugin adapter routes according to the adapter configuration
     */
    private void addPluginRoutes(final Plugin plugin) throws Exception { //NOSONAR
        if (plugin.getAggregator() != null) {
            camelContext.addRoutes(newAggregatorRouteBuilder(plugin));
        } else if (plugin.getProcessor() != null) {
            camelContext.addRoutes(newProcessorRouteBuilder(plugin));
        }
    }

    /**
     * Invoke init methods upon the plugin
     */
    protected void initPlugin(Plugin plugin) throws Exception { //NOSONAR
        if (pluginCanConsume(plugin)) {
            getPluginInitializer().init(plugin);
            plugin.getContext().setSchedulerBuilder(schedulerBuildersFactory.build(plugin));
            plugin.getContext().getSchedulerBuilder().schedule();
        }
    }

    /**
     * Perform the plugin uninitialization
     */
    protected void unInitPlugin(Plugin plugin) throws Exception { //NOSONAR
        plugin.getContext().setShuttingDown(true);
        plugin.getContext().getSchedulerBuilder().unschedule();
    }

    /**
     * stop the routes for the plugin
     */
    @Override
    protected void stopRoutes(final Plugin plugin) throws Exception { //NOSONAR
        super.stopRoutes(plugin);
        final Map<String, Set<String>> consumersMap = getPluginsConsumersMap();
        for (String fromId : consumersMap.keySet()) {
            final String fromUri = (fromId == null) ? inputUri : pluginsMap.get(fromId).getContext().getEndpoints().getOutputUri();
            String[] consumers = calcConsumers(consumersMap.get(fromId));
            gracefullyRemoveRoute(camelContext, routeId(fromUri, consumers));
        }
        gracefullyRemoveRoute(camelContext, routeId(routeId(plugin.getContext().getEndpoints().getOutputUri(), outputUri)));
        if (plugin.getAggregator() != null) {
            newAggregatorRouteBuilder(plugin).removeRoutes();
        } else if (plugin.getProcessor() != null) {
            newProcessorRouteBuilder(plugin).removeRoutes();
        }
    }

    /**
     * Initialize the processor plugin route builder
     */
    protected BasicRoutesBuilder newProcessorRouteBuilder(Plugin plugin) throws Exception { //NOSONAR
        return getBuildersFactory().newProcessorPluginRouteBuilder(
                camelContext, plugin
        );
    }

    /**
     * Initialize the aggregator plugin route builder
     */
    protected BasicRoutesBuilder newAggregatorRouteBuilder(Plugin plugin) throws Exception { //NOSONAR
        return getBuildersFactory().newAggregatorPluginRouteBuilder(
                camelContext, plugin);
    }
}
