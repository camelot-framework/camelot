package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.component.jms.JmsBinding;
import org.apache.camel.component.jms.JmsMessage;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.beans.Dad;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * User: lanqu
 * Date: 19.12.12
 */
public class PluggableProcessorTest {

    final String DAD_ON_WITH_MAP = "DAD_ON_WITH_MAP";
    private PluggableProcessor pluggableProcessor;
    private ApplicationContext context;
    private Exchange exchange;
    private Message message;
    private PluggableProcessorTest mockedThis;

    @Processor
    public String on(Dad o, Map map) {
        return DAD_ON_WITH_MAP;
    }

    @Before
    public void setUp() {
        final BasicMessagesSerializer serializer = spy(BasicMessagesSerializer.class);
        exchange = mock(Exchange.class);
        message = spy(new JmsMessage(mock(javax.jms.Message.class), mock(JmsBinding.class)));
        context = mock(ApplicationContext.class);
        mockedThis = mock(PluggableProcessorTest.class);

        when(message.getHeaders()).thenReturn(new HashMap<String, Object>());
        when(exchange.getIn()).thenReturn(message);
        message.setBody(serializer.processBodyAndHeadersBeforeSend(new Dad(), message.getHeaders(), getClass().getClassLoader()));
        when(context.getAutowireCapableBeanFactory()).thenReturn(mock(ConfigurableListableBeanFactory.class));
        when(mockedThis.on(any(Dad.class), any(Map.class))).thenCallRealMethod();
        pluggableProcessor = new PluggableProcessor(PluggableProcessorTest.class, mockedThis, serializer);
    }

    @Test
    public void testPluggableProcessor() throws NoSuchMethodException {
        pluggableProcessor.process(exchange);
        verify(mockedThis).on(any(Dad.class), any(Map.class));
        verify(message, atLeast(2)).getBody();
        verify(message, atLeast(2)).getHeaders();
        verify(message, atLeast(1)).setBody(any(Dad.class));
        verify(message, atLeast(1)).setBody(eq(DAD_ON_WITH_MAP));
        verifyNoMoreInteractions(mockedThis);
    }

    @Test
    public void testNoExceptionWhenNullBody() throws NoSuchMethodException {
        when(message.getBody()).thenReturn(null);
        pluggableProcessor.process(exchange);
        verify(message, atLeast(2)).getBody();
        verify(message).setBody(eq(null));
        verifyNoMoreInteractions(mockedThis);
    }
}
