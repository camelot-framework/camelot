package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoTailingQueue;
import ru.qatools.mongodb.TailingQueue;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.common.PluggableProcessor;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.Plugin;

import javax.annotation.PostConstruct;
import java.io.Serializable;

import static java.text.MessageFormat.format;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.apache.camel.LoggingLevel.DEBUG;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov
 */
public class MongodbDirectRoutesInitializer implements CamelContextAware {
    public static final String URI_PREFIX = "direct://mongodb";
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbDirectRoutesInitializer.class);
    private final PluginsService pluginsService;
    private final MongoClient mongoClient;
    private final String dbName;
    private final int poolSize;
    private final long maxSize;
    private CamelContext camelContext;

    public MongodbDirectRoutesInitializer(PluginsService pluginsService, MongoClient mongoClient,
                                          String dbName, int poolSize, long maxSize) {
        this.pluginsService = pluginsService;
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.maxSize = maxSize;
        this.poolSize = poolSize;
    }

    private static MongoQueueMessage msg(Object event, Plugin plugin) {
        return new MongoQueueMessage(plugin.getId(), event);
    }

    private static String calcColName(String inputUri) {
        return inputUri.replace(".", "_");
    }

    private static String calcFromUri(String inputUri) {
        return inputUri.replace(URI_PREFIX, "").split("\\?")[0];
    }

    @Override
    public CamelContext getCamelContext() {
        return null;
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
            if (inputUri.startsWith(URI_PREFIX)) {
                LOGGER.info("Initializing MongoDB direct route for {}", plugin.getId());
                final String colName = calcColName(inputUri);
                final MongoTailingQueue<MongoQueueMessage> queue = new MongoTailingQueue<>(
                        MongoQueueMessage.class, mongoClient, dbName, colName, maxSize
                );
                queue.init();
                try {
                    addSaverRoute(camelContext, plugin, endpoints, colName, queue);
                    initPoller(camelContext, plugin, inputUri, queue);
                } catch (Exception e) {
                    throw new RuntimeException(format("Failed to initialize MongoDB direct route for %s!",//NOSONAR
                            plugin.getId()), e);
                }

            }
        });
    }

    private void initPoller(CamelContext camelContext, Plugin plugin, String inputUri, MongoTailingQueue<MongoQueueMessage> queue) {
        LOGGER.info("Initializing MongoDB queue poller for {}", plugin.getId());
        try {
            final EventProducer producer = initEventProducer(camelContext, inputUri,
                    pluginsService.getMessagesSerializer());
            newFixedThreadPool(poolSize).submit(() ->
                    queue.poll(m -> producer.produce(m.object, map(
                            BODY_CLASS, m.object.getClass().getName(),
                            PLUGIN_ID, m.pluginId
                    ))));
        } catch (Exception e) {
            throw new RuntimeException(format("Failed to initialize MongoDB poller for %s!",//NOSONAR
                    plugin.getId()), e);
        }
    }

    private void addSaverRoute(CamelContext camelContext, final Plugin plugin, PluginEndpoints endpoints,
                               final String colName, final MongoTailingQueue<MongoQueueMessage> queue) throws Exception {
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
        final TailingQueue<MongoQueueMessage> queue;
        final Plugin plugin;

        Saver(TailingQueue<MongoQueueMessage> queue, Plugin plugin) {
            this.queue = queue;
            this.plugin = plugin;
        }

        @Processor
        public void save(Serializable event) {
            queue.add(msg(event, plugin));
        }
    }

}
