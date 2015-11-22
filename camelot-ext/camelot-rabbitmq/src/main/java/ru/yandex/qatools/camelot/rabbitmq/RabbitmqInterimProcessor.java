package ru.yandex.qatools.camelot.rabbitmq;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.common.InterimProcessor;

import java.util.HashSet;

/**
 * @author Ilya Sadykov
 */
public class RabbitmqInterimProcessor implements InterimProcessor {

    @Override
    public void process(Exchange exchange) throws Exception { //NOSONAR
        // Cleaning up the kafka headers as they must be set by kafka producer from scratch
        new HashSet<>(exchange.getIn().getHeaders().keySet()).stream()
               .filter(header -> header.startsWith("ru/yandex/qatools/camelot/rabbitmq")).forEach(
                header -> exchange.getIn().removeHeader(header));
    }
}
