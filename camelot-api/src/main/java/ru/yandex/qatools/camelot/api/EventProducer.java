package ru.yandex.qatools.camelot.api;

import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface EventProducer {
    /**
     * Produce the event for the defined queue with the list of the headers
     */
    void produce(Object event, Map<String, Object> headers);

    /**
     * Produce the event for the defined queue
     */
    void produce(Object event);

    /**
     * Produce the event for the defined queue with the header value
     */
    void produce(Object event, String header, Object headerValue);
}
