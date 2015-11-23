package ru.yandex.qatools.camelot.kafka;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.common.InterimProcessor;

/**
 * @author Ilya Sadykov
 */
public class KafkaInterimProcessor implements InterimProcessor {

    @Override
    public void process(Exchange exchange) throws Exception { //NOSONAR
        // Cleaning up the kafka headers as they must be set by kafka producer from scratch
        exchange.getIn().getHeaders().keySet().stream()
                .filter(header -> header.startsWith("kafka"))
                .forEach(header -> exchange.getIn().removeHeader(header));
    }
}
