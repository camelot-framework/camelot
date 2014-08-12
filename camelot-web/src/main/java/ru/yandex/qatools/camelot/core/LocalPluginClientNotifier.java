package ru.yandex.qatools.camelot.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.config.PluginWebContext;
import ru.yandex.qatools.camelot.core.impl.PluggableProcessor;

import java.io.Serializable;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalPluginClientNotifier extends PluggableProcessor {
    protected Notifier processorImpl;

    public LocalPluginClientNotifier() {
        super(Notifier.class);
    }

    public LocalPluginClientNotifier(Notifier processorImpl) {
        super(Notifier.class, processorImpl);
        this.processorImpl = processorImpl;
    }

    public static class Notifier {
        static final Logger logger = LoggerFactory.getLogger(LocalPluginClientNotifier.class);

        @Autowired
        WebfrontEngine pluginsService;

        @InjectHeader(BODY_CLASS)
        String bodyClassName;

        @InjectHeader(PLUGIN_ID)
        String pluginId;

        @InjectHeader(TOPIC)
        String topic;

        @Processor
        @SuppressWarnings("unchecked")
        public Object notify(Object message) throws Exception {
            final PluginWebContext pluginContext = pluginsService.getPluginContext(pluginId);
            if (message instanceof byte[]) {
                try {
                    final ClassLoader classLoader = pluginContext.getClassLoader();
                    Class<? extends Serializable> bodyClass = (Class<? extends Serializable>) classLoader.loadClass(bodyClassName);
                    message = deserializeFromBytes((byte[]) message, classLoader, bodyClass);
                } catch (Exception e) {
                    logger.warn("Failed to deserialize message from bytes", e);
                }
            }
            logger.debug("Local client notifier: " + pluginId + " : " + message);
            pluginContext.getLocalBroadcastersProvider().getBroadcaster(topic).send(message);
            return message;
        }
    }

}
