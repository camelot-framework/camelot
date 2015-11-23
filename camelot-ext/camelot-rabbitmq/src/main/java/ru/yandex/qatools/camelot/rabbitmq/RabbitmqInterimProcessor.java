package ru.yandex.qatools.camelot.rabbitmq;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.common.InterimProcessor;

import static java.util.stream.Collectors.toList;

/**
 * @author Ilya Sadykov
 */
public class RabbitmqInterimProcessor implements InterimProcessor {

    @Override
    public void process(Exchange exchange) throws Exception { //NOSONAR
        // Cleaning up the rabbitmq headers as they must be set by rabbitmq producer from scratch
        exchange.getIn().getHeaders().keySet().stream().collect(toList()).stream()
                .filter(header -> header.startsWith("rabbitmq"))
                .forEach(header -> exchange.getIn().removeHeader(header));
    }
}
