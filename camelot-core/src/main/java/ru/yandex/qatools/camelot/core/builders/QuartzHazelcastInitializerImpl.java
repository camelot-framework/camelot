package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import org.quartz.Scheduler;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class QuartzHazelcastInitializerImpl extends QuartzInitializerImpl {

    public static final String DEFAULT_QUARTZ_LOCK = "defaultQuartzLock";

    private final HazelcastInstance hazelcastInstance;

    public QuartzHazelcastInitializerImpl(HazelcastInstance hazelcastInstance, Scheduler scheduler) {
        super(scheduler);
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * Returns the Quartz lock within HazelCast
     */
    @Override
    public synchronized ILock getLock() {
        if (lock == null) {
            lock = hazelcastInstance.getLock(DEFAULT_QUARTZ_LOCK);
        }
        return (ILock) lock;
    }

    @Override
    public void unlock() {
        getLock().forceUnlock();
    }
}
