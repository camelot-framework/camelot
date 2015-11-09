package ru.yandex.qatools.camelot.mongodb;

import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.config.Plugin;

import java.lang.reflect.Method;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MongodbSyncAnnotatedMethodInvoker<A> extends PluginAnnotatedMethodInvoker<A> {
    final MongoPessimisticRepo mongoRepo;
    private final int timeoutSec;
    private final boolean unlockAfter;

    public MongodbSyncAnnotatedMethodInvoker(MongoPessimisticRepo mongoRepo, Plugin plugin, Class anClass,
                                             int timeoutSec, boolean unlockAfter) {
        super(plugin, anClass);
        this.mongoRepo = mongoRepo;
        this.timeoutSec = timeoutSec;
        this.unlockAfter = unlockAfter;
    }

    @Override
    public void invoke(Method method, Object... args) {
        final String key = lockKey(method, args);
        try {
            mongoRepo.getLock().tryLock(key, SECONDS.toMillis(timeoutSec));
            super.invoke(method, args);
        } catch (Exception e) {
            LOGGER.warn(format("Failed to wait for the lock to invoke method %s of plugin %s",
                    method.getName(), plugin.getId()), e);
        } finally {
            if (unlockAfter) {
                try {
                    mongoRepo.getLock().unlock(key);
                } catch (Exception e) {
                    LOGGER.warn("Failed to unlock the key {} for invocation", key, e);
                }
            }
        }
    }

    private String lockKey(Method method, Object[] args) {
        return format("%s%s%s", plugin.getId(), method.getName(), join(args, ","));
    }
}
