package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoException;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultExchangeHolder;
import org.apache.camel.spi.AggregationRepository;
import org.apache.camel.spi.OptimisticLockingAggregationRepository;
import org.apache.camel.support.ServiceSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoPessimisticLocking;
import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.qatools.mongodb.error.LockWaitTimeoutException;
import ru.qatools.mongodb.error.PessimisticException;
import ru.yandex.qatools.camelot.api.error.RepositoryFailureException;
import ru.yandex.qatools.camelot.api.error.RepositoryLockWaitException;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithLocks;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithValuesMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class MongodbAggregationRepository extends ServiceSupport
        implements AggregationRepository,
        OptimisticLockingAggregationRepository,
        AggregationRepositoryWithLocks,
        AggregationRepositoryWithValuesMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongodbAggregationRepository.class);

    private final String repoName;
    private final MongoPessimisticLocking mongoLocking;
    private final long waitForLockSec;

    private MongoPessimisticRepo<DefaultExchangeHolder> mongoRepo;

    public MongodbAggregationRepository(String repoName, MongoPessimisticLocking mongoLocking, long waitForLockSec) {
        this.repoName = repoName;
        this.mongoLocking = mongoLocking;
        this.waitForLockSec = waitForLockSec;
    }

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        try {
            debug("Getting exchange for key '{}'", key);
            return toExchange(camelContext, mongoRepo.tryLockAndGet(key, SECONDS.toMillis(waitForLockSec)));
        } catch (LockWaitTimeoutException e) {
            throw new RepositoryLockWaitException(format(
                    "Failed to acquire the lock for the key '%s' within timeout of %ds",
                    key, waitForLockSec), e);
        } catch (PessimisticException | MongoException e) {
            throw new RepositoryUnreachableException(e);
        }
    }

    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange oldExchange, Exchange newExchange) {
        return add(camelContext, key, newExchange);
    }

    @Override
    public Exchange add(final CamelContext camelContext, final String key, final Exchange exchange) {
        return writeAttempt(key, () -> {
            DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);
            mongoRepo.putAndUnlock(key, holder);
            return toExchange(camelContext, holder);
        });
    }

    @Override
    public void remove(CamelContext camelContext, final String key, final Exchange exchange) {
        writeAttempt(key, () -> {
            mongoRepo.removeAndUnlock(key);
            return exchange;
        });
    }

    @Override
    public Exchange getWithoutLock(CamelContext camelContext, String key) {
        return toExchange(camelContext, mongoRepo.get(key));
    }

    @Override
    public void lock(String key) {
        try {
            debug("Locking key '{}'", key);
            mongoLocking.tryLock(key, SECONDS.toMillis(waitForLockSec));
        } catch (LockWaitTimeoutException e) {
            throw new RepositoryLockWaitException(format(
                    "Failed to acquire the lock for the key '%s' within timeout of %ds",
                    key, waitForLockSec), e);
        } catch (PessimisticException | MongoException e) {
            LOGGER.warn("Failed to lock the key: ", e);
            throw new RepositoryUnreachableException(e);
        }
    }

    @Override
    public void unlockQuietly(String key) {
        try {
            mongoLocking.unlock(key);
        } catch (Exception e) {
            LOGGER.trace("Sonar trick", e);
            debug("Failed to quiet unlock repo key '{}'", key, e);
        }
    }

    @Override
    public void unlock(String key) {
        try {
            debug("Unlocking the key '{}'", key);
            mongoLocking.unlock(key);
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
        return unmodifiableSet(new LinkedHashSet<>(mongoRepo.keySet()));
    }

    @Override
    public void doStart() throws Exception { //NOSONAR
        mongoRepo = new MongoPessimisticRepo<>(mongoLocking);
    }

    @Override
    public void doStop() throws Exception { //NOSONAR
        /* Nothing to do */
    }

    private Exchange writeAttempt(String key, Callable<Exchange> perform) {
        try {
            return perform.call();
        } catch (LockWaitTimeoutException e) {
            throw new RepositoryLockWaitException(format(
                    "Failed to acquire the lock for the key '%s' within timeout of %ds",
                    key, waitForLockSec), e);
        } catch (PessimisticException | MongoException e) {
            LOGGER.warn("Failed to put the key: ", e);
            throw new RepositoryUnreachableException(e);
        } catch (Exception e) {
            error("Failed to update map for key '{}'", key, e);
            throw new RepositoryFailureException(format("Failed to get exchange for key '%s'", key), e);
        }
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
            mongoLocking.forceUnlock(key);
        } catch (Exception e) {
            error("Failed to force unlock the key '{}'", key, e);
        }
    }

    private void debug(String message, String key) {
        LOGGER.debug("[{}] " + message, repoName, key);
    }

    private void debug(String message, String key, Exception e) {
        LOGGER.debug("[{}] " + message + " because of: {}", repoName, key, e.toString());
    }

    private void warn(String message, String key, Exception e) {
        LOGGER.warn("[{}] " + message + " because of: {}", repoName, key, e.toString());
    }

    private void error(String message, String key, Exception e) {
        LOGGER.error("[{}] " + message, repoName, key, e);
    }

    @Override
    public Map<String, Exchange> values(CamelContext camelContext) {
        Map<String, DefaultExchangeHolder> holderMap = new LinkedHashMap<>(mongoRepo.keyValueMap());
        Map<String, Exchange> result = new HashMap<>(holderMap.size(), 1);
        for (Map.Entry<String, DefaultExchangeHolder> entry : holderMap.entrySet()) {
            result.put(entry.getKey()   , toExchange(camelContext, entry.getValue()));
        }
        return unmodifiableMap(result);
    }
}
