package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.quorum.QuorumException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultExchangeHolder;
import org.apache.camel.spi.AggregationRepository;
import org.apache.camel.spi.OptimisticLockingAggregationRepository;
import org.apache.camel.support.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.error.RepositoryFailureException;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MINUTES;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

public class HazelcastAggregationRepository extends ServiceSupport implements AggregationRepository,
        OptimisticLockingAggregationRepository, AggregationRepositoryWithLocks {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HazelcastInstance hazelcastInstance;
    private String repository;
    private IMap<String, DefaultExchangeHolder> map;

    private long waitForLockSec = MINUTES.toSeconds(5);

    @Override
    public void doStart() throws Exception {
        map = hazelcastInstance.getMap(repository);
    }

    @Override
    public void doStop() throws Exception {
        /* Nothing to do */
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
        try {
            debug("Adding new exchange, updating map.tryPut('{}')...", key);
            DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);
            map.tryPut(key, holder, waitForLockSec, TimeUnit.SECONDS);
            return toExchange(camelContext, holder);
        } catch (Exception e) {
            error("Failed to update map for key '{}", e, key);
            throw new RepositoryFailureException("Failed to get exchange for key '" + key + "'", e);
        } finally {
            debug("Unlocking key map.forceUnlock('{}')...", key);
            try {
                map.forceUnlock(key);
            } catch (Exception e) {
                error("Failed to force unlock the exchange for key '{}'", e, key);
            }
        }
    }

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        try {
            debug("Getting from context. map.get('{}')...", key);
            if (map.tryLock(key, waitForLockSec, TimeUnit.SECONDS)) {
                return toExchange(camelContext, map.get(key));
            }
            logger.error("Failed to acquire the lock for the key within timeout of " + waitForLockSec + "s");
            return null;
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (Exception e) {
            error("Failed to get the exchange for key '{}'", e, key);
            throw new RepositoryFailureException("Failed to get exchange for key '" + key + "'", e);
        }
    }

    @Override
    public Exchange getWithoutLock(CamelContext camelContext, String key) {
        return toExchange(camelContext, map.get(key));
    }

    private Exchange toExchange(CamelContext camelContext, DefaultExchangeHolder holder) {
        Exchange exchange = null;
        if (holder != null) {
            exchange = new DefaultExchange(camelContext);
            DefaultExchangeHolder.unmarshal(exchange, holder);
        }
        return exchange;
    }

    @Override
    public void lock(String key) {
        try {
            debug("Locking key map.tryLock('{}')...", key);
            if (!map.tryLock(key, waitForLockSec, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to lock within timeout of " + waitForLockSec + "s");
            }
        } catch (Exception e) {
            error("Failed to lock the key '{}'", e, key);
        }
    }

    @Override
    public void unlock(String key) {
        try {
            debug("Forcing the unlock of key map.forceUnlock('{}')...", key);
            map.forceUnlock(key);
        } catch (Exception e) {
            error("Failed to unlock the key '{}'", e, key);
        }
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange oldExchange, Exchange newExchange) throws OptimisticLockingException {
        return add(camelContext, key, newExchange);
    }

    @Override
    public void remove(CamelContext camelContext, String key, Exchange exchange) {
        try {
            debug("Removing key map.tryRemove('{}')...", key);
            if (map.containsKey(key) && !map.tryRemove(key, waitForLockSec, TimeUnit.SECONDS)) {
                throw new RuntimeException("Failed to remove the exchange within timeout of " + waitForLockSec + "s");
            }
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (Exception e) {
            error("Failed to remove the exchange for key '{}'", e, key);
        } finally {
            debug("Forcing unlock map.forceUnlock('{}')", key);
            try {
                map.forceUnlock(key);
            } catch (Exception e) {
                error("Failed to force unlock the exchange for key '{}'", e, key);
            }
        }
    }

    @Override
    public void confirm(CamelContext camelContext, String key) {
        debug("Forcing unlock map.forceUnlock('{}')", key);
        try {
            map.forceUnlock(key);
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (Exception e) {
            error("Failed to force unlock the exchange for key '{}'", e, key);
        }
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void setWaitForLockSec(long waitForLockSec) {
        this.waitForLockSec = waitForLockSec;
    }

    protected IMap getMap() {
        return map;
    }


    private void debug(final String message, String key) {
        logger.debug("[{}] " + message, repository, key);
    }

    private void error(final String message, Exception e, String key) {
        logger.error(format("[{}] " + message + ": \n{}",
                repository, key, formatStackTrace(e), e));
    }
}
