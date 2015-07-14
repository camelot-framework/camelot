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
import ru.yandex.qatools.camelot.api.error.RepositoryDirtyWriteAttemptException;
import ru.yandex.qatools.camelot.api.error.RepositoryFailureException;
import ru.yandex.qatools.camelot.api.error.RepositoryLockWaitException;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.yandex.qatools.camelot.util.DateUtil.isTimePassedSince;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

public class HazelcastAggregationRepository
        extends ServiceSupport
        implements AggregationRepository,
                   OptimisticLockingAggregationRepository,
                   AggregationRepositoryWithLocks {

    public static final String LAST_UPDATE_TS = "LAST_UPDATE_TS";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HazelcastInstance hazelcastInstance;
    private String repository;
    private IMap<String, DefaultExchangeHolder> map;

    private long lockWaitHeartbeatSec = 5; // 5 seconds between lock trials
    private long waitForLockSec = MINUTES.toSeconds(5);

    @Override
    public Exchange add(final CamelContext camelContext, final String key, final Exchange exchange) {
        return writeAttempt(camelContext, exchange, key, new Callable<Exchange>() {
            @Override
            public Exchange call() throws Exception {
                DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);
                if (map.tryPut(key, holder, waitForLockSec, SECONDS)) {
                    map.unlock(key);
                    return toExchange(camelContext, holder);
                }
                throw new RepositoryLockWaitException(format(
                        "Failed to acquire the lock for the key '%s' within timeout of %ds",
                        key, waitForLockSec));
            }
        });
    }

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        try {
            debug("Getting from context. map.get('{}')...", key);
            if (tryLock(key)) {
                return toExchange(camelContext, map.get(key));
            }
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (InterruptedException e) {
            throw new RepositoryFailureException(format(
                    "Failed to lock exchange for key '%s'", key), e);
        }
        throw new RepositoryLockWaitException(format(
                "Failed to acquire the lock for the key '%s' within timeout of %ds",
                key, waitForLockSec));
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange oldExchange, Exchange newExchange)
            throws OptimisticLockingException {
        return add(camelContext, key, newExchange);
    }

    @Override
    public void remove(CamelContext camelContext, final String key, final Exchange exchange) {
        writeAttempt(camelContext, exchange, key, new Callable<Exchange>() {
            @Override
            public Exchange call() throws Exception {
                if (map.tryRemove(key, waitForLockSec, SECONDS)) {
                    return exchange;
                }
                throw new RepositoryLockWaitException("Failed to remove the exchange within timeout of " + waitForLockSec + "s");
            }
        });
    }

    @Override
    public Exchange getWithoutLock(CamelContext camelContext, String key) {
        return toExchange(camelContext, map.get(key));
    }

    @Override
    public void lock(String key) {
        try {
            debug("Locking key map.tryLock('{}')...", key);
            if (tryLock(key)) {
                return;
            }
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (InterruptedException e) {
            throw new RepositoryFailureException(format(
                    "Failed to lock exchange for key '%s'", key), e);
        }
        throw new RepositoryLockWaitException(format(
                "Failed to acquire the lock for the key '%s' within timeout of %ds",
                key, waitForLockSec));
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
    public void confirm(CamelContext camelContext, String key) {
        forceUnlockKey(key);
    }

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public void doStart() throws Exception {
        map = hazelcastInstance.getMap(repository);
    }

    @Override
    public void doStop() throws Exception {
        /* Nothing to do */
    }

    private Exchange writeAttempt(CamelContext camelContext, Exchange exchange, String key, Callable<Exchange> perform) {
        try {
            debug("Performing write attempt...", key);
            final Exchange oldExchange = toExchange(camelContext, map.get(key));
            if (compareLastUpdateTimes(exchange, oldExchange)) {
                exchange.getIn().setHeader(LAST_UPDATE_TS, currentTimeMillis());
                return perform.call();
            }
        } catch (QuorumException e) {
            throw new RepositoryUnreachableException("Hazelcast is out of Quorum!", e);
        } catch (RepositoryLockWaitException e) {
            throw e;
        } catch (Exception e) {
            error("Failed to update map for key '{}'", e, key);
            throw new RepositoryFailureException("Failed to get exchange for key '" + key + "'", e);
        }
        throw new RepositoryDirtyWriteAttemptException("Failed to perform update for key '" + key + "'!");
    }

    private long getLastUpdateTs(Exchange oldValue) {
        final Object value = (oldValue != null && oldValue.getIn() != null) ?
                oldValue.getIn().getHeader(LAST_UPDATE_TS) : null;
        return (value != null) ? (long) value : 0;
    }

    private boolean compareLastUpdateTimes(Exchange exchange, Exchange oldExchange) {
        final long old = getLastUpdateTs(oldExchange);
        final long current = getLastUpdateTs(exchange);
        return old < current || (old == 0 && current == 0);
    }


    private boolean tryLock(String key) throws InterruptedException {
        long startedTime = currentTimeMillis();
        boolean timeout = false;
        while (!map.tryLock(key, lockWaitHeartbeatSec, SECONDS) && !timeout) {
            debug("Lock is still not available, waiting for key {}...", key);
            timeout = isTimePassedSince(SECONDS.toMillis(waitForLockSec), startedTime);
        }
        return !timeout;
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

    public void setLockWaitHeartbeatSec(long lockWaitHeartbeat) {
        this.lockWaitHeartbeatSec = lockWaitHeartbeat;
    }

    public void setWaitForLockSec(long waitForLockSec) {
        this.waitForLockSec = waitForLockSec;
    }

    protected IMap getMap() {
        return map;
    }

    private Exchange toExchange(CamelContext camelContext, DefaultExchangeHolder holder) {
        Exchange exchange = null;
        if (holder != null) {
            exchange = new DefaultExchange(camelContext);
            DefaultExchangeHolder.unmarshal(exchange, holder);
        }
        return exchange;
    }

    private void forceUnlockKey(String key) {
        debug("Forcing unlock map.forceUnlock('{}')", key);
        try {
            map.forceUnlock(key);
        } catch (Exception e) {
            error("Failed to force unlock the exchange for key '{}'", e, key);
        }
    }

    private void debug(final String message, String key) {
        logger.debug("[{}] " + message, repository, key);
    }

    private void error(final String message, Exception e, String key) {
        logger.error("[{}] " + message + ": \n{}", repository, key, formatStackTrace(e), e);
    }
}
