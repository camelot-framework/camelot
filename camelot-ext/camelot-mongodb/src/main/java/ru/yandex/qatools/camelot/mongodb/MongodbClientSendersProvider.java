package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoTailingQueue;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.common.PluginUriBuilder;

import java.io.Serializable;
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
    private static final int DEFAULT_POOL_SIZE = 10;
    private final ExecutorService senderPool;
    private final MongoTailingQueue<MongoQueueMessage> queue;
    private final MessagesSerializer serializer;
    private final String feBroadcastUri;
    private CamelContext camelContext;

    public MongodbClientSendersProvider(MessagesSerializer serializer, PluginUriBuilder uriBuilder,
                                        MongoClient mongoClient, String dbName, String colName, long maxSize) {
        this(serializer, uriBuilder, DEFAULT_POOL_SIZE, mongoClient, dbName, colName, maxSize);
    }

    public MongodbClientSendersProvider(MessagesSerializer serializer, PluginUriBuilder uriBuilder, int poolSize,
                                        MongoClient mongoClient, String dbName, String colName, long maxSize) {
        this.queue = new MongoTailingQueue<>(MongoQueueMessage.class, mongoClient, dbName, colName, maxSize);
        queue.init();
        this.serializer = serializer;
        this.feBroadcastUri = uriBuilder.frontendBroadcastUri();
        this.senderPool = newFixedThreadPool(poolSize);
    }

    @Override
    public ClientMessageSender getSender(String topic, String pluginId, String feNotifyUri) {
        return (t, m) -> senderPool.submit(
                () -> queue.add(new MongoQueueMessage(pluginId, m, topic))
        );
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
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
                            .to(feBroadcastUri);
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
                    ))));
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MongoDB poller for client notify support!", e);//NOSONAR
        }
    }

    public static class MongoQueueMessage implements Serializable {
        final Serializable object;
        final String topic;
        final String pluginId;

        public MongoQueueMessage(String pluginId, Object object, String topic) {
            if (!(object instanceof Serializable)) {
                throw new RuntimeException("Could not send message '" + object + "': it's not serializable!");//NOSONAR
            }
            this.pluginId = pluginId;
            this.object = (Serializable) object;
            this.topic = topic;
        }
    }
}
