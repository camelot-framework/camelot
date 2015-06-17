package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author smecsia
 */
public class MemoryAggregationRepository extends org.apache.camel.processor.aggregate.MemoryAggregationRepository
        implements AggregationRepositoryWithLocks {
    private static final Logger LOGGER = getLogger(MemoryAggregationRepository.class);
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();
    private final long waitForLockSec;
    private final Plugin plugin;

    public MemoryAggregationRepository(Plugin plugin, long waitForLockSec) {
        this.waitForLockSec = waitForLockSec;
        this.plugin = plugin;
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange oldExchange, Exchange newExchange) {
        return add(camelContext, key, newExchange);
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
        try {
            LOGGER.info(format("[%s] Adding the state. add/unlock(%s)", plugin.getId(), key));
            return super.add(camelContext, key, exchange);
        } catch (Exception e) {
            LOGGER.error("Failed to add the key " + key + "! ", e);
            return null;
        } finally {
            unlock(key);
        }
    }

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        try {
            LOGGER.info(format("[%s] Getting the state. tryLock(%s)", plugin.getId(), key));
            getLock(key).tryLock(waitForLockSec, SECONDS);
            final Exchange exchange = super.get(camelContext, key);
            return (exchange == null) ? null : exchange.copy();
        } catch (Exception e) {
            LOGGER.error("Failed to get the key " + key + "! Forcing to unlock...", e);
            unlock(key);
        }
        return null;
    }

    @Override
    public void remove(CamelContext camelContext, String key, Exchange exchange) {
        try {
            LOGGER.info(format("[%s] Removing the state. unlock(%s)", plugin.getId(), key));
            super.remove(camelContext, key, exchange);
        } catch (Exception e) {
            LOGGER.error("Failed to remove the key " + key + "! ", e);
        } finally {
            unlock(key);
        }
    }

    @Override
    public void confirm(CamelContext camelContext, String exchangeId) {
        unlock(exchangeId);
    }

    protected Lock getLock(String key) {
        synchronized (locks) {
            if (!locks.containsKey(key)) {
                locks.put(key, new ReentrantLock());
            }
            return locks.get(key);
        }
    }

    @Override
    public Exchange getWithoutLock(CamelContext camelContext, String key) {
        return super.get(camelContext, key);
    }

    @Override
    public void unlock(String key) {
        try {
            getLock(key).unlock();
        } catch (Exception e) {
            LOGGER.error("Failed to unlock key " + key, e);
        }
    }

    @Override
    public void lock(String key) {
        try {
            getLock(key).lock();
        } catch (Exception e) {
            LOGGER.error("Failed to lock key " + key, e);
        }
    }
}
