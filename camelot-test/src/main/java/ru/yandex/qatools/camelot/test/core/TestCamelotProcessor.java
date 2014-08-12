package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.impl.CamelotProcessor;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestCamelotProcessor extends CamelotProcessor {
    final Object procMock;
    final Processor original;

    public TestCamelotProcessor(ClassLoader classLoader, Class procClass, Object procMock, Processor original, Plugin plugin) {
        super(classLoader, procClass, plugin.getContext());
        this.original = original;
        this.procMock = procMock;
    }

    @Override
    public void process(Exchange message) {
        Object event = message.getIn().getBody();
        event = processAfterIn(event, (String) message.getIn().getHeader(BODY_CLASS));
        try {
            dispatchMessage(procMock, event, message.getIn().getHeaders());
            original.process(message);
        } catch (Exception e) {
            throw new RuntimeException("Could not dispatch to the mocked processor: ", e);
        }
    }
}
