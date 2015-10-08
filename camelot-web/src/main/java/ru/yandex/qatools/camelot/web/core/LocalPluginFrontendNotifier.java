package ru.yandex.qatools.camelot.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.common.PluggableProcessor;
import ru.yandex.qatools.camelot.config.PluginWebContext;

import static ru.yandex.qatools.camelot.api.Constants.Headers.*;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalPluginFrontendNotifier extends PluggableProcessor {
    protected Notifier processorImpl;

    public LocalPluginFrontendNotifier(MessagesSerializer serializer) {
        super(Notifier.class, serializer);
    }

    public LocalPluginFrontendNotifier(Notifier processorImpl, MessagesSerializer serializer) {
        super(Notifier.class, processorImpl, serializer);
        this.processorImpl = processorImpl;
    }

    public static class Notifier {
        static final Logger logger = LoggerFactory.getLogger(LocalPluginFrontendNotifier.class);

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
        public Object notify(Object message) throws Exception { //NOSONAR
            final PluginWebContext pluginContext = pluginsService.getPluginContext(pluginId);
            if (message instanceof byte[]) {
                try {
                    final ClassLoader classLoader = pluginContext.getClassLoader();
                    message = deserializeFromBytes((byte[]) message, classLoader); //NOSONAR
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
