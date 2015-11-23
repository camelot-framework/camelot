package ru.yandex.qatools.camelot.rabbitmq;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov
 */
public class RabbitmqInterimProcessorTest {
    @Test
    public void testFilterRabbitMqHeaders() throws Exception {
        Exchange exchange = mock(Exchange.class);
        Message in = mock(Message.class);
        when(exchange.getIn()).thenReturn(in);
        when(in.getHeaders()).thenReturn(map(
                "rabbitmq.HEADER1", "value1",
                "rabbitmq.HEADER2", "value2",
                "another.HEADER", "value3"
        ));
        new RabbitmqInterimProcessor().process(exchange);
        verify(in).removeHeader("rabbitmq.HEADER1");
        verify(in).removeHeader("rabbitmq.HEADER2");
    }
}