package ru.yandex.qatools.camelot.kafka;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.common.InterimProcessor;

import java.util.HashSet;

/**
 * @author Ilya Sadykov
 */
public class KafkaInterimProcessor implements InterimProcessor {

    @Override
    public void process(Exchange exchange) throws Exception { //NOSONAR
        // Cleaning up the kafka headers as they must be set by kafka producer from scratch
        for (String header : new HashSet<>(exchange.getIn().getHeaders().keySet())) {
            if (header.startsWith("kafka")) {
                exchange.getIn().removeHeader(header);
            }
        }
    }
}
