package ru.yandex.qatools.camelot.core.util;

import org.apache.camel.CamelContext;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.core.impl.AppConfigSystemProperties;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;
import static ru.yandex.qatools.camelot.core.util.ReflectUtil.resolveResourcesFromPattern;
import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ServiceUtil {

    ServiceUtil() {
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
