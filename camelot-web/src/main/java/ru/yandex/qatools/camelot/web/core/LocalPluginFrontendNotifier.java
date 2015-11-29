package ru.yandex.qatools.camelot.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.common.PluggableProcessor;

import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalPluginFrontendNotifier extends PluggableProcessor {

    public LocalPluginFrontendNotifier(MessagesSerializer serializer) {
        super(Notifier.class, serializer);
    }

    public static class Notifier {
        static final Logger logger = LoggerFactory.getLogger(LocalPluginFrontendNotifier.class);

        @Autowired
        WebfrontEngine pluginsService;

        @InjectHeader(PLUGIN_ID)
        String pluginId;

        @InjectHeader(TOPIC)
        String topic;

        @Processor
        public Object notify(Object message) throws Exception { //NOSONAR
            logger.debug("Local client notifier: " + pluginId + " : " + message);
            pluginsService.getPluginContext(pluginId)
                    .getLocalBroadcastersProvider().getBroadcaster(topic).send(message);
            return message;
        }
    }

}
