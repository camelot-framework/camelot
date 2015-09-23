package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.beans.IncomingMessage;
import ru.yandex.qatools.camelot.core.MessagesSerializer;

import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class IncomingMessagesQueueProcessor extends PluggableProcessor {
    public IncomingMessagesQueueProcessor(CamelContext camelContext, MessagesSerializer serializer) {
        super(Proc.class, new Proc(camelContext,serializer), serializer);
    }

    public static class Proc {
        CamelContext camelContext;
        MessagesSerializer serializer;

        public Proc(CamelContext camelContext, MessagesSerializer serializer) {
            this.camelContext = camelContext;
            this.serializer = serializer;
        }

        @Processor
        public void process(IncomingMessage message) throws Exception {
            initEventProducer(camelContext, message.getInputUri(), serializer).produce(message.getMessage());
        }
    }
}
