package ru.yandex.qatools.camelot.core;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

/**
 * @author smecsia
 */
public interface AggregationRepositoryWithLocks {
    /**
     * Get the exchange without locking
     */
    Exchange getWithoutLock(CamelContext camelContext, String key);

    /**
     * Unlock the key
     */
    void unlockQuietly(String key);

    /**
     * Unlock the key
     */
    void unlock(String key);

    /**
     * Lock the key
     */
    void lock(String key);
}
