package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.beans.IncomingMessage;

import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class IncomingMessagesQueueProcessor extends PluggableProcessor {
    public IncomingMessagesQueueProcessor(CamelContext camelContext) {
        super(Proc.class, new Proc(camelContext));
    }

    public static class Proc {
        CamelContext camelContext;

        public Proc(CamelContext camelContext) {
            this.camelContext = camelContext;
        }

        @Processor
        public void process(IncomingMessage message) throws Exception {
            initEventProducer(camelContext, message.getInputUri()).produce(message.getMessage());
        }
    }
}
