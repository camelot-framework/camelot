package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.config.Plugin;

import java.lang.reflect.Method;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class HazelcastSyncAnnotatedMethodInvoker<A> extends PluginAnnotatedMethodInvoker<A> {
    final HazelcastInstance hazelcastInstance;
    private final int timeoutSec;
    private final boolean unlockAfter;

    public HazelcastSyncAnnotatedMethodInvoker(HazelcastInstance hazelcastInstance, Plugin plugin, Class anClass,
                                               int timeoutSec, boolean unlockAfter) {
        super(plugin, anClass);
        this.hazelcastInstance = hazelcastInstance;
        this.timeoutSec = timeoutSec;
        this.unlockAfter = unlockAfter;
    }

    @Override
    public void invoke(Method method, Object... args) {
        final ILock lock = getLock(method, args);
        try {
            if (lock.tryLock(timeoutSec, SECONDS)) {
                super.invoke(method, args);
            }
        } catch (Exception e) {
            LOGGER.warn(format("Failed to wait for the lock to invoke method %s of plugin %s",
                    method.getName(), plugin.getId()), e);
        } finally {
            if (unlockAfter) {
                lock.forceUnlock();
            }
        }
    }

    protected ILock getLock(Method method, Object[] args) {
        return hazelcastInstance.getLock(format("%s%s%s", plugin.getId(), method.getName(), join(args, ",")));
    }
}
