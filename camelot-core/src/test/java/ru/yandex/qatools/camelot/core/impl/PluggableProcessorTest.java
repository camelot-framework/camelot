package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.core.beans.Dad;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * User: lanqu
 * Date: 19.12.12
 */
public class PluggableProcessorTest {

    final String DAD_ON_WITH_MAP = "DAD_ON_WITH_MAP";

    @Processor
    public String on(Dad o, Map map) {
        return DAD_ON_WITH_MAP;
    }

    private PluggableProcessor pluggableProcessor;
    private ApplicationContext context;
    private Exchange exchange;
    private Message message;
    private PluggableProcessorTest mockedThis;
    private Dad body;

    @Before
    public void setUp() {
        exchange = mock(Exchange.class);
        message = mock(Message.class);
        context = mock(ApplicationContext.class);
        mockedThis = mock(PluggableProcessorTest.class);
        body = new Dad();

        when(exchange.getIn()).thenReturn(message);
        when(message.getBody()).thenReturn(body);
        when(message.getHeaders()).thenReturn(new HashMap<String, Object>());
        when(context.getAutowireCapableBeanFactory()).thenReturn(mock(ConfigurableListableBeanFactory.class));
        when(mockedThis.on(any(Dad.class), any(Map.class))).thenCallRealMethod();
        pluggableProcessor = new PluggableProcessor(PluggableProcessorTest.class, mockedThis);
    }

    @Test
    public void testPluggableProcessor() throws NoSuchMethodException {
        pluggableProcessor.process(exchange);
        verify(message).getBody();
        verify(message).getHeader(eq(BODY_CLASS));
        verify(message).getHeaders();
        verify(message).setHeader(eq(BODY_CLASS), any(String.class));
        verify(message).setBody(eq(DAD_ON_WITH_MAP));
        verifyNoMoreInteractions(message);
        verify(mockedThis).on(same(body), any(Map.class));
        verifyNoMoreInteractions(mockedThis);
    }

    @Test
    public void testNoExceptionWhenNullBody() throws NoSuchMethodException {
        when(message.getBody()).thenReturn(null);
        pluggableProcessor.process(exchange);
        verify(message).getBody();
        verify(message).getHeader(eq(BODY_CLASS));
        verify(message).setBody(eq(null));
        verifyNoMoreInteractions(message);
        verifyNoMoreInteractions(mockedThis);
    }
}
