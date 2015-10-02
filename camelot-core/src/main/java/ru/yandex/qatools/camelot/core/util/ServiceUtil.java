package ru.yandex.qatools.camelot.core.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.core.impl.AppConfigSystemProperties;
import ru.yandex.qatools.camelot.core.impl.BasicEventProducer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;
import static ru.yandex.qatools.camelot.core.util.ReflectUtil.resolveResourcesFromPattern;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ServiceUtil {

    ServiceUtil() {
    }

    /**
     * Search for the annotated field within the procClass
     * and initialize the Camel producer for the uri
     */
    public static EventProducer initEventProducer(CamelContext camelContext, final String uri, final MessagesSerializer serializer)
            throws Exception { //NOSONAR
        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.setDefaultEndpointUri(uri);
        return new BasicEventProducer(producerTemplate) {
            @Override
            public void produce(Object event, Map<String, Object> headers) {
                try {
                    final ClassLoader eventCL = event.getClass().getClassLoader();
                    final ClassLoader contextCL = currentThread().getContextClassLoader();
                    final ClassLoader cl = (eventCL != null) ? eventCL : ((contextCL != null) ? contextCL : getSystemClassLoader());
                    producerTemplate.sendBodyAndHeaders(
                            serializer.processBodyAndHeadersBeforeSend(event, headers, cl), headers);
                } catch (Exception e) {
                    logger.error("Failed to produce message to the uri " + uri, e);
                }
            }
        };
    }

    /**
     * Initializes the client sender to the topic with uri
     */
    public static ClientMessageSender initEventSender(
            CamelContext camelContext, String uri, final String topic,
            final MessagesSerializer serializer) throws Exception { //NOSONAR
        final EventProducer producer = initEventProducer(camelContext, uri, serializer);
        return new ClientMessageSender() {
            @Override
            public void send(Object message) {
                send(topic, message);
            }

            @Override
            public void send(String topic, Object message) {
                producer.produce(message, TOPIC, topic);
            }
        };
    }

    /**
     * Initialize the plugin source app configuration
     */
    public static AppConfig initPluginAppConfig( //NOSONAR
            AppConfig baseConfig, ClassLoader loader, String path)
            throws IOException, ClassNotFoundException, URISyntaxException {
        final Properties properties = new Properties();
        for (Resource resource : resolveResourcesFromPattern(path, loader)) {
            properties.load(IOUtils.readResource(resource.getURL()));
        }
        return wrapPluginAppConfig(baseConfig, properties);
    }

    /**
     * Wrap application config with properties
     */
    public static AppConfig wrapPluginAppConfig(final AppConfig baseConfig,
                                                final Properties properties) {
        return new AppConfigSystemProperties() {
            @Override
            public String getProperty(String key) {
                final String res = baseConfig.getProperty(key);
                return (res != null) ? res : properties.getProperty(key);
            }
        };
    }
}
