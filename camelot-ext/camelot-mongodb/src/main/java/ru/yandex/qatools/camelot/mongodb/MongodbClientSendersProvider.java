package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoTailableQueue;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.common.PluginUriBuilder;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static ru.yandex.qatools.camelot.api.Constants.Headers.*;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov
 */
public class MongodbClientSendersProvider implements ClientSendersProvider, CamelContextAware {
    private static final String MONGODB_FRONTEND_URI = "direct:mongodb.frontend.notify";
    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbClientSendersProvider.class);
    private final ExecutorService senderPool;
    private final MongoClient mongoClient;
    private final String dbName;
    private final String colName;
    private final long maxSize;
    private final MessagesSerializer serializer;
    private final String feBroadcastUri;
    private final int minPollIntervalMs;
    private final MongoSerializerBuilder serializerBuilder;
    private MongoTailableQueue<MongoQueueMessage> queue;
    private CamelContext camelContext;

    public MongodbClientSendersProvider(MessagesSerializer serializer, MongoSerializerBuilder serializerBuilder,
                                        PluginUriBuilder uriBuilder, int poolSize,
                                        MongoClient mongoClient, String dbName, String colName, long maxSize,
                                        int minPollIntervalMs) { //NOSONAR
        this.serializer = serializer;
        this.serializerBuilder = serializerBuilder;
        this.feBroadcastUri = uriBuilder.frontendBroadcastUri();
        this.senderPool = newFixedThreadPool(poolSize);
        this.mongoClient = mongoClient;
        this.dbName = dbName;
        this.colName = colName;
        this.maxSize = maxSize;
        this.minPollIntervalMs = minPollIntervalMs;
    }

    @Override
    public ClientMessageSender getSender(String topic, String pluginId, String feNotifyUri) {
        return new ClientMessageSender() {
            @Override
            public void send(Object message) {
                send(topic, message);
            }

            @Override
            public void send(String topic, Object message) {
                senderPool.submit(
                        () -> queue.add(new MongoQueueMessage(pluginId, message, topic))
                );
            }
        };
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        this.queue = new MongoTailableQueue<>(MongoQueueMessage.class, mongoClient, dbName, colName, maxSize);
        final MongoSerializer mongoSerializer = serializerBuilder.build(this.serializer, MongoQueueMessage.class.getClassLoader());
        queue.setDeserializer(mongoSerializer);
        queue.setSerializer(mongoSerializer);
        queue.setMinPollIntervalMs(minPollIntervalMs);
        queue.init();
        initPoller(camelContext);
        initRoutes(camelContext);
    }

    private void initRoutes(CamelContext camelContext) {
        try {
            LOGGER.info("Initializing MongoDB queue routes");
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(MONGODB_FRONTEND_URI).
                            log(LoggingLevel.DEBUG, "MongoDB frontend notify message {headers.bodyClass}")
                            .to(feBroadcastUri).routeId(MONGODB_FRONTEND_URI);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize additional routes for MongoDB client notify support!", e); //NOSONAR
        }
    }

    private void initPoller(CamelContext camelContext) {
        LOGGER.info("Initializing MongoDB queue poller");
        try {
            final EventProducer producer = initEventProducer(camelContext, MONGODB_FRONTEND_URI, serializer);
            newSingleThreadExecutor().submit(() ->
                    queue.poll(m -> producer.produce(m.object, map(
                            BODY_CLASS, m.object.getClass().getName(),
                            TOPIC, m.topic,
                            PLUGIN_ID, m.pluginId
                    )))
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoDB poller for client notify support!", e);//NOSONAR
        }
    }
}
