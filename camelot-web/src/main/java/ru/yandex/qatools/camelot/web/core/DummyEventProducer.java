package ru.yandex.qatools.camelot.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.EventProducer;

import java.util.HashMap;
import java.util.Map;

import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class DummyEventProducer implements EventProducer {
    Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void produce(Object event, Map<String, Object> headers) {
        logger.warn("Dropping event due to delayed Camel initialization: " + event);
    }

    @Override
    public void produce(Object event) {
        produce(event, new HashMap<String, Object>());
    }

    @Override
    public void produce(Object event, String header, Object headerValue) {
        produce(event, map(header, headerValue));
    }
}
