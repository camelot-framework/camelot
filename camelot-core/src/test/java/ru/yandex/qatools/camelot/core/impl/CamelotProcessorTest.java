package ru.yandex.qatools.camelot.core.impl;

import org.junit.Test;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.PluginContextInjector;
import ru.yandex.qatools.camelot.error.DispatchException;
import ru.yandex.qatools.fsm.annotations.OnException;

import java.security.AccessControlException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotProcessorTest {

    public static class Proc {

        @Processor
        public String procString(String body) {
            throw new IllegalArgumentException("Illegal argument!");
        }

        @Processor
        public String procInt(Integer body) {
            throw new AccessControlException("Access denied");
        }

        @OnException
        public Object onError(AccessControlException e, Object body) {
            return body;
        }

        @OnException(preserve = true)
        public Object onError(RuntimeException e, Object body) {
            return body;
        }

    }

    @Test
    public void testProcessor() throws DispatchException {
        Proc prcMock = new Proc();
        PluginContext context = new PluginContext();
        context.setInjector(mock(PluginContextInjector.class));
        CamelotProcessor proc = new CamelotProcessor(getClass().getClassLoader(), Proc.class, prcMock, context);
        final Map<String, Object> headers = Collections.emptyMap();
        assertThat((String) proc.dispatchMessage(prcMock, "testEvent", headers), equalTo("testEvent"));
        assertThat(proc.dispatchMessage(prcMock, 10, headers), nullValue());
    }

}
