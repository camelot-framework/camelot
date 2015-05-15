package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.StartupListener;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.ShutdownPrepared;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginsConfig;
import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.core.PluginInitializer;
import ru.yandex.qatools.camelot.core.PluginLoader;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.builders.BasicRoutesBuilder;
import ru.yandex.qatools.camelot.core.builders.NoSchedulerBuildersFactory;
import ru.yandex.qatools.camelot.core.builders.QuartzInitializer;
import ru.yandex.qatools.camelot.core.builders.SchedulerBuildersFactory;

import java.util.*;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.DEBUG;
import static ru.yandex.qatools.camelot.Constants.TMP_INPUT_BUFFER_URI;
import static ru.yandex.qatools.camelot.util.NameUtil.routeId;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveRoute;
import static ru.yandex.qatools.camelot.util.ServiceUtil.initTmpInputBufferProducer;

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

        startLoading();

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
            buildConsumersRoutes(getPluginsMap());
        } catch (Exception e) {
            logger.error("Could not build consumers routes from plugins", e);
        }

        try {
            if (getScheduler() != null) {
                this.quartzInitializer = getBuildersFactory().newQuartzInitializer(scheduler);
                this.quartzInitializer.start();
            }
        } catch (Exception e) {
            logger.error("Could not start the scheduler", e);
        }
        try {
            camelContext.addStartupListener(new StartupListener() {
                @Override
                public void onCamelContextStarted(CamelContext context, boolean alreadyStarted) throws Exception {
                    for (Plugin plugin : getPluginsMap().values()) {
                        initPlugin(plugin);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Failed to add camel context listener", e);
        }
        stopLoading();
    }


    @Override
    public synchronized void stop() {
        for (Plugin plugin : getPluginsMap().values()) {
            try {
                // stopping the endpoints for the plugin
                plugin.getContext().getMainInput().stop();
                plugin.getContext().getInput().stop();
                plugin.getContext().getOutput().stop();
                // first we'll redefine the endpoints to the temporary queue
                plugin.getContext().setMainInput(initTmpInputBufferProducer(camelContext, plugin.getContext().getEndpoints().getMainInputUri(), TMP_INPUT_BUFFER_URI));
                plugin.getContext().setInput(initTmpInputBufferProducer(camelContext, plugin.getContext().getEndpoints().getInputUri(), TMP_INPUT_BUFFER_URI));
                plugin.getContext().setOutput(initTmpInputBufferProducer(camelContext, plugin.getContext().getEndpoints().getOutputUri(), TMP_INPUT_BUFFER_URI));
                unInitPlugin(plugin);
            } catch (Exception e) {
                logger.error("Failed to stop route for plugin " + plugin.getId(), e);
            }
        }
        super.stop();
    }

    @Override
    public synchronized void reloadAndStart() {
        super.reloadAndStart();
        startLoading();
        try {
            for (Plugin plugin : getPluginsMap().values()) {
                stopRoutes(plugin);
            }
            for (PluginsConfig config : getConfigs()) {
                buildRoutes(config);
            }
            buildConsumersRoutes(getPluginsMap());
        } catch (Exception e) {
            logger.error("Failed to reload plugins!", e);
        }
        stopLoading();
    }

    @Override
    public void prepareShutdown(boolean forced) {
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
    private void buildRoutes(final PluginsConfig config) throws Exception {
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
    private void buildConsumersRoutes(final Map<String, Plugin> pluginsMap) throws Exception {
        final Map<String, Set<String>> consumersMap = getPluginsConsumersMap(pluginsMap);
        // Add route to populate all the completed tests events to all the registered plugins or to stop
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // Producer plugins messages go to the consumers from INPUT or other plugin
                for (String fromId : consumersMap.keySet()) {
                    final String fromUri = (fromId == null) ? inputUri : pluginsMap.get(fromId).getContext().getEndpoints().getOutputUri();
                    String[] consumers = calcConsumers(fromId, consumersMap.get(fromId));
                    from(fromUri)
                            .multicast()
                            .executorServiceRef(CAMELOT_MULTICAST_PROFILE)
                            .parallelProcessing()
                            .log(DEBUG, "Event from " + fromUri + " to [" + StringUtils.join(consumers, ",") + "]: ${in.header.bodyClass}")
                            .to(consumers)
                            .routeId(routeId(fromUri, consumers));
                }
                // The rest of the plugins messages go to OUTPUT
                for (String fromId : pluginsMap.keySet()) {
                    final Plugin plugin = pluginsMap.get(fromId);
                    if (!consumersMap.keySet().contains(fromId) && pluginCanConsume(plugin)) {
                        final PluginEndpoints endpoints = plugin.getContext().getEndpoints();
                        from(endpoints.getOutputUri())
                                .routeId(endpoints.getOutputRouteId())
                                .multicast()
                                .executorServiceRef(CAMELOT_MULTICAST_PROFILE)
                                .parallelProcessing()
                                .to((isListenersEnabled()) ?
                                        new String[]{outputUri, endpoints.getEndpointListenerUri()} :
                                        new String[]{outputUri});
                    }
                }
            }
        });
    }

    /**
     * Calculate the consumers array from the set of consumers for the plugin (or null (as INPUT))
     */
    private String[] calcConsumers(String fromId, Set<String> consumers) {
        final Collection consumersSet = CollectionUtils.collect(consumers, new Transformer() {
            @Override
            public Object transform(Object pluginId) {
                return getPluginsMap().get(pluginId).getContext().getEndpoints().getInputUri();
            }
        });
        if (fromId != null && isListenersEnabled()) {
            final PluginEndpoints endpoints = getPluginsMap().get(fromId).getContext().getEndpoints();
            consumersSet.add(endpoints.getEndpointListenerUri());
        }
        return (String[]) (consumersSet).toArray(new String[consumersSet.size()]);
    }

    /**
     * Calc consumer plugins map for each plugin
     */
    private Map<String, Set<String>> getPluginsConsumersMap(final Map<String, Plugin> pluginsMap) {
        Map<String, Set<String>> result = new HashMap<>();
        for (final Plugin plugin : pluginsMap.values()) {
            if (pluginCanConsume(plugin)) {
                final String from = plugin.getSource();
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
    private void addPluginRoutes(final Plugin plugin) throws Exception {
        if (plugin.getAggregator() != null) {
            camelContext.addRoutes(newAggregatorRouteBuilder(plugin));
        } else if (plugin.getProcessor() != null) {
            camelContext.addRoutes(newProcessorRouteBuilder(plugin));
        }
    }

    /**
     * Invoke init methods upon the plugin
     */
    protected void initPlugin(Plugin plugin) throws Exception {
        if (pluginCanConsume(plugin)) {
            getPluginInitializer().init(plugin);
            plugin.getContext().setSchedulerBuilder(schedulerBuildersFactory.build(plugin));
            plugin.getContext().getSchedulerBuilder().schedule();
        }
    }

    /**
     * Perform the plugin uninitialization
     */
    protected void unInitPlugin(Plugin plugin) throws Exception {
        plugin.getContext().setShuttingDown(true);
        plugin.getContext().getSchedulerBuilder().unschedule();
    }

    /**
     * stop the routes for the plugin
     */
    @Override
    protected void stopRoutes(final Plugin plugin) throws Exception {
        super.stopRoutes(plugin);
        final Map<String, Set<String>> consumersMap = getPluginsConsumersMap(getPluginsMap());
        for (String fromId : consumersMap.keySet()) {
            final String fromUri = (fromId == null) ? inputUri : pluginsMap.get(fromId).getContext().getEndpoints().getOutputUri();
            String[] consumers = calcConsumers(fromId, consumersMap.get(fromId));
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
    protected BasicRoutesBuilder newProcessorRouteBuilder(Plugin plugin) throws Exception {
        return getBuildersFactory().newProcessorPluginRouteBuilder(
                camelContext, plugin
        );
    }

    /**
     * Initialize the aggregator plugin route builder
     */
    protected BasicRoutesBuilder newAggregatorRouteBuilder(Plugin plugin) throws Exception {
        return getBuildersFactory().newAggregatorPluginRouteBuilder(
                camelContext, plugin);
    }

}
