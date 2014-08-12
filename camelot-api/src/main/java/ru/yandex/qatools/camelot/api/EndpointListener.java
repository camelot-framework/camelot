package ru.yandex.qatools.camelot.api;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface EndpointListener {
    /**
     * Add the processor
     */
    void listen(long timeout, TimeUnit unit, Processor proc) throws InterruptedException;

    interface Processor<T> {

        /**
         * On message from endpoint
         * Returns the flag indicating whatever or not we should still wait or not
         */
        boolean onMessage(T message, Map<String, Object> headers);
    }
}
