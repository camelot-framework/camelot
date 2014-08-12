package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.OperationTimeoutException;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.spi.ShutdownPrepared;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.locks.Lock;

public class HazelcastQuartzSchedulerStartupListener implements ShutdownPrepared, ApplicationListener {

    public static final String DEFAULT_QUARTZ_LOCK = "defaultQuartzLock";
    Logger log = LoggerFactory.getLogger(getClass());
    Lock lock;

    protected volatile boolean initialized = false;
    protected String lockName;
    protected HazelcastInstance hazelcastInstance;
    protected QuartzComponent quartzComponent;

    public HazelcastQuartzSchedulerStartupListener() {
        super();
        log.info("HazelcastQuartzSchedulerStartupListener created");
    }

    public void setLockName(final String lockName) {
        this.lockName = lockName;
    }

    public synchronized Lock getLock() {
        if (lock == null) {
            lock = hazelcastInstance.getLock(lockName != null ? lockName : DEFAULT_QUARTZ_LOCK);
        }
        return lock;
    }

    @Override
    public void prepareShutdown(boolean forced) {
        unlock();
    }

    @Required
    public void setQuartzComponent(QuartzComponent quartzComponent) {
        this.quartzComponent = quartzComponent;
    }

    @Required
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public synchronized void onApplicationEvent(ApplicationEvent event) {
        if (initialized) {
            return;
        }
        try {
            while (true) {
                try {
                    getLock().lock();
                    initialized = true;
                    log.warn("This node is now the master Quartz!");
                    try {
                        quartzComponent.startScheduler();
                    } catch (Exception e) {
                        unlock();
                        throw new RuntimeException("Failed to start scheduler", e);
                    }
                    return;
                } catch (OperationTimeoutException e) {
                    log.warn("This node is not the master Quartz and failed to wait for the lock!", e);
                }
            }
        } catch (Exception e) {
            log.error("Error while trying to wait for the lock from Hazelcast!", e);
        }
    }

    private synchronized void unlock() {
        try {
            getLock().unlock();
        } catch (IllegalStateException e) {
            log.warn("Exception while trying to unlock quartz lock: Hazelcast instance is already inactive!", e);
        } catch (Exception e) {
            log.warn("Exception during the unlock of the master Quartz!", e);
        }
    }
}
