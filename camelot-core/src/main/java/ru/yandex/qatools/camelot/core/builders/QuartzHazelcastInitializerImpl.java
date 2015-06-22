package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;

import static java.lang.System.currentTimeMillis;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class QuartzHazelcastInitializerImpl extends QuartzInitializerImpl {

    public static final String DEFAULT_QUARTZ_LOCK = "defaultQuartzLock";
    public static final String HEARTBEAT_LAST_TIME = "defaultQuartzHeartBeatTime";

    private final IAtomicLong lastHeartBeatTime;
    private final HazelcastInstance hazelcastInstance;

    public QuartzHazelcastInitializerImpl(HazelcastInstance hazelcastInstance, Scheduler scheduler, AppConfig config) {
        super(scheduler, config);
        this.hazelcastInstance = hazelcastInstance;
        this.lastHeartBeatTime = hazelcastInstance.getAtomicLong(HEARTBEAT_LAST_TIME);
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

    @Override
    public long getLastHartbeat() {
        return lastHeartBeatTime.get();
    }

    @Override
    public void updateHeartBeat() {
        lastHeartBeatTime.set(currentTimeMillis());
    }

    @Override
    public boolean isMaster() {
        return getLock().isLockedByCurrentThread();
    }
}
