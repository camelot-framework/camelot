package ru.yandex.qatools.camelot.common;

import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.EventProducer;

import java.util.HashMap;
import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;

/**
 * @author smecsia
 */
public class BasicEventProducer implements EventProducer {
    final protected Logger LOGGER = LoggerFactory.getLogger(getClass());//NOSONAR
    final ProducerTemplate producerTemplate;

    public BasicEventProducer(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @Override
    public void produce(Object event, Map<String, Object> headers) {
        headers.put(BODY_CLASS, event.getClass().getName());
        producerTemplate.sendBodyAndHeaders(serializeToBytes(event), headers);
    }

    @Override
    public void produce(Object event) {
        produce(event, new HashMap<>());
    }

    @Override
    public void produce(Object event, String header, Object headerValue) {
        produce(event, map(header, headerValue));
    }
}
