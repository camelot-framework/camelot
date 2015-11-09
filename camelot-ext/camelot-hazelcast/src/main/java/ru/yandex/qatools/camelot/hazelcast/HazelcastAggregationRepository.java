package ru.yandex.qatools.camelot.hazelcast;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultExchangeHolder;
import org.apache.camel.spi.AggregationRepository;
import org.apache.camel.spi.OptimisticLockingAggregationRepository;
import org.apache.camel.support.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.error.*;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithLocks;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.yandex.qatools.camelot.util.DateUtil.isTimePassedSince;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class HazelcastAggregationRepository extends ServiceSupport
        implements AggregationRepository,
        OptimisticLockingAggregationRepository,
        AggregationRepositoryWithLocks {

    private static final TimeBasedGenerator UUID_GENERATOR
            = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

    private static final String LAST_UPDATE_UUID_HEADER = "LAST_UPDATE_UID";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private HazelcastInstance hazelcastInstance;
    private String repository;
    private IMap<String, DefaultExchangeHolder> map;

    private long lockWaitHeartbeatSec = 5; // 5 seconds between lock trials
    private long waitForLockSec = MINUTES.toSeconds(5);

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        try {
            debug("Getting exchange for key '{}'", key);
            if (tryLock(key)) {
                debug("Successfully locked the key '{}', (read)", key);
                return toExchange(camelContext, map.get(key));
            }
        } catch (IllegalStateException e) {
            error("Hazelcast is in invalid state while processing key '{}'", key, e);
            throw new RepositoryNeedRestartException(e);
        } catch (HazelcastException e) {
            throw new RepositoryUnreachableException(e);
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
    public Exchange add(final CamelContext camelContext, final String key, final Exchange exchange) {
        return writeAttempt(camelContext, exchange, key, new Callable<Exchange>() {
            @Override
            public Exchange call() throws Exception {
                DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);
                map.put(key, holder);
                return toExchange(camelContext, holder);
            }
        });
    }

    @Override
    public void remove(CamelContext camelContext, final String key, final Exchange exchange) {
        writeAttempt(camelContext, exchange, key, new Callable<Exchange>() {
            @Override
            public Exchange call() throws Exception {
                map.remove(key);
                return exchange;
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
            debug("Locking key '{}'", key);
            if (tryLock(key)) {
                return;
            }
        } catch (IllegalStateException e) {
            error("Hazelcast is in invalid state while processing key '{}'", key, e);
            throw new RepositoryNeedRestartException(e);
        } catch (HazelcastException e) {
            throw new RepositoryUnreachableException(e);
        } catch (InterruptedException e) {
            throw new RepositoryFailureException(format(
                    "Failed to lock exchange for key '%s'", key), e);
        }
        throw new RepositoryLockWaitException(format(
                "Failed to acquire the lock for the key '%s' within timeout of %ds",
                key, waitForLockSec));
    }

    @Override
    public void unlockQuietly(String key) {
        try {
            map.unlock(key);
        } catch (Exception e) {
            logger.trace("Sonar trick", e);
            debug("Failed to quiet unlock repo key '{}'", key, e);
        }
    }


    @Override
    public void unlock(String key) {
        try {
            debug("Unlocking the key '{}'", key);
            map.unlock(key);
            debug("Successfully unlocked the key '{}'", key);
        } catch (IllegalMonitorStateException e) { //NOSONAR
            warn("Failed to unlock the key '{}'", key, e);
        } catch (Exception e) {
            error("Failed to unlock the key '{}'", key, e);
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
    public void doStart() throws Exception { //NOSONAR
        map = hazelcastInstance.getMap(repository);
    }

    @Override
    public void doStop() throws Exception { //NOSONAR
        /* Nothing to do */
    }

    private Exchange writeAttempt(CamelContext camelContext, Exchange exchange, String key,
                                  Callable<Exchange> perform) {
        boolean lockSuccess = true;
        try {
            debug("Performing write attempt for key '{}'", key);
            if (lockSuccess = tryLock(key)) { //NOSONAR
                debug("Successfully locked the key '{}' (write)", key);
                if (exchangeNotUpdated(exchange, toExchange(camelContext, map.get(key)))) {
                    setLastUpdateUuidHeader(exchange);
                    return perform.call();
                }
            }
        } catch (IllegalStateException e) {
            error("Hazelcast is in invalid state while processing key '{}'", key, e);
            throw new RepositoryNeedRestartException(e);
        } catch (HazelcastException e) {
            throw new RepositoryUnreachableException(e);
        } catch (Exception e) {
            error("Failed to update map for key '{}'", key, e);
            throw new RepositoryFailureException(format("Failed to get exchange for key '%s'", key), e);
        } finally {
            unlock(key);
        }

        if (!lockSuccess) {
            throw new RepositoryLockWaitException(format(
                    "Failed to lock exchange for the key '%s' within timeout of %ds",
                    key, waitForLockSec));
        }

        throw new RepositoryDirtyWriteAttemptException(format(
                "Failed to perform update for key '%s': entry was updated in the meantime!",
                key));
    }

    private boolean exchangeNotUpdated(Exchange exchange, Exchange oldExchange) {
        return Objects.equals(getLastUpdateUuid(exchange), getLastUpdateUuid(oldExchange));
    }

    private Object getLastUpdateUuid(Exchange exchange) {
        return exchange != null ? exchange.getProperty(LAST_UPDATE_UUID_HEADER) : null;
    }

    private void setLastUpdateUuidHeader(Exchange exchange) {
        exchange.setProperty(LAST_UPDATE_UUID_HEADER, UUID_GENERATOR.generate());
    }

    private boolean tryLock(String key) throws InterruptedException {
        long startedTime = currentTimeMillis();
        boolean timeout = false;
        while (!map.tryLock(key, lockWaitHeartbeatSec, SECONDS) && !timeout) {
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
        debug("Forcing unlock for key '{}'", key);
        try {
            map.forceUnlock(key);
        } catch (Exception e) {
            error("Failed to force unlock the key '{}'", key, e);
        }
    }

    private void debug(String message, String key) {
        logger.debug("[{}] " + message, repository, key);
    }

    private void debug(String message, String key, Exception e) {
        logger.debug("[{}] " + message + " because of: {}", repository, key, e.toString());
    }

    private void warn(String message, String key, Exception e) {
        logger.warn("[{}] " + message + " because of: {}", repository, key, e.toString());
    }

    private void error(String message, String key, Exception e) {
        logger.error("[{}] " + message, repository, key, e);
    }
}
