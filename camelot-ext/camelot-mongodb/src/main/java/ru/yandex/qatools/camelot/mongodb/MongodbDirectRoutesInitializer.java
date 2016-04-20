package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoTailableQueue;
import ru.qatools.mongodb.TailableQueue;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.common.PluggableProcessor;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.Plugin;

import javax.annotation.PostConstruct;
import java.io.Serializable;

import static java.text.MessageFormat.format;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.apache.camel.LoggingLevel.DEBUG;
import static ru.yandex.qatools.camelot.Constants.INPUT_SUFFIX;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov
 */
public class MongodbDirectRoutesInitializer implements CamelContextAware {
    public static final String URI_PREFIX = "mongodb://topic";
    public static final String NEW_INPUT_URI_PREFIX = "direct://mongodb.topic.";
    public static final String COL_SUFFIX = "_direct_queue";
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbDirectRoutesInitializer.class);
    private final PluginsService pluginsService;
    private final MongoClient mongoClient;
    private final String dbName;
    private final MongoSerializerBuilder serializerBuilder;
    private final long maxSize;
    private final int minPollIntervalMs;
    private CamelContext camelContext;

    public MongodbDirectRoutesInitializer(PluginsService pluginsService, MongoClient mongoClient,
                                          String dbName, MongoSerializerBuilder serializerBuilder, long maxSize, int minPollIntervalMs) {
        this.pluginsService = pluginsService;
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.maxSize = maxSize;
        this.minPollIntervalMs = minPollIntervalMs;
        this.serializerBuilder = serializerBuilder;
    }

    private static MongoQueueMessage msg(Object event, Plugin plugin) {
        return new MongoQueueMessage(plugin.getId(), event);
    }

    private static String calcColName(Plugin plugin) {
        return plugin.getId() + COL_SUFFIX;
    }

    private static String calcFromUri(String inputUri) {
        return inputUri.replace(URI_PREFIX, "").split("\\?")[0];
    }

    public static String overridenUri(Plugin plugin, String suffix) {
        return NEW_INPUT_URI_PREFIX + plugin.getId() + "." + suffix;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @PostConstruct
    public void init() {
        initRoutes(camelContext);
    }

    private void initRoutes(CamelContext camelContext) {
        LOGGER.info("Initializing MongoDB direct routes");
        pluginsService.getPluginsMap().values().forEach(plugin -> {
            final PluginEndpoints endpoints = plugin.getContext().getEndpoints();
            final String inputUri = endpoints.getInputUri();
            if (inputUri.startsWith(NEW_INPUT_URI_PREFIX)) {
                LOGGER.info("Initializing MongoDB direct route for {}", plugin.getId());
                final String colName = calcColName(plugin);
                final MongoTailableQueue<MongoQueueMessage> queue = initQueue(colName, plugin.getContext().getClassLoader());
                queue.setMinPollIntervalMs(minPollIntervalMs);
                queue.init();
                try {
                    addSaverRoute(camelContext, plugin, endpoints, colName, queue);
                    initPoller(camelContext, plugin, queue);
                } catch (Exception e) {
                    throw new RuntimeException(format("Failed to initialize MongoDB direct route for %s!",//NOSONAR
                            plugin.getId()), e);
                }

            }
        });
    }

    private MongoTailableQueue<MongoQueueMessage> initQueue(String colName, ClassLoader classLoader) {
        final MongoTailableQueue<MongoQueueMessage> queue = new MongoTailableQueue<>(
                MongoQueueMessage.class, mongoClient, dbName, colName, maxSize
        );
        final MongoSerializer serializer = serializerBuilder.build(pluginsService.getMessagesSerializer(), classLoader);
        queue.setDeserializer(serializer);
        queue.setSerializer(serializer);
        return queue;
    }

    private void initPoller(CamelContext camelContext, Plugin plugin, MongoTailableQueue<MongoQueueMessage> queue) {
        LOGGER.info("Initializing MongoDB queue poller for {}", plugin.getId());
        try {
            final EventProducer producer = initEventProducer(camelContext, overridenUri(plugin, INPUT_SUFFIX),
                    pluginsService.getMessagesSerializer());
            newSingleThreadExecutor().submit(() ->
                    queue.poll(m -> {
                        if (pluginsService.isInitialized()) {
                            producer.produce(m.object, map(
                                    BODY_CLASS, m.object.getClass().getName(),
                                    PLUGIN_ID, m.pluginId
                            ));
                        }
                    }));
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to initialize MongoDB poller for %s!",//NOSONAR
                    plugin.getId()), e);
        }
    }

    private void addSaverRoute(CamelContext camelContext, final Plugin plugin, PluginEndpoints endpoints,
                               final String colName, final MongoTailableQueue<MongoQueueMessage> queue)
            throws Exception {//NOSONAR
        final String fromUri = calcFromUri(endpoints.getConsumerUri());
        final String consumerUri = endpoints.getConsumerUri();
        final String consumerRouteId = endpoints.getConsumerRouteId();
        camelContext.removeRouteDefinition(camelContext.getRouteDefinition(consumerRouteId));
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                interimProc(plugin, from(consumerUri)
                        .log(DEBUG, format("===> ROUTE %s ===> [MongoDB].%s", fromUri, colName)))
                        .process(
                                new PluggableProcessor(Saver.class, new Saver(queue, plugin),
                                        pluginsService.getMessagesSerializer())
                        ).stop().routeId(consumerRouteId);
            }
        });
    }

    protected <T extends ProcessorDefinition> T interimProc(Plugin plugin, T route) {
        if (plugin.getContext().getInterimProcessor() != null) {
            route.process(plugin.getContext().getInterimProcessor());
        }
        return route;
    }

    public static class Saver {
        final TailableQueue<MongoQueueMessage> queue;
        final Plugin plugin;

        Saver(TailableQueue<MongoQueueMessage> queue, Plugin plugin) {
            this.queue = queue;
            this.plugin = plugin;
        }

        @Processor
        public void save(Serializable event) {
            queue.add(msg(event, plugin));
        }
    }

}
