package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.ILock;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.AbstractQuartzInitializer;

import static java.lang.System.currentTimeMillis;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class QuartzHazelcastInitializer extends AbstractQuartzInitializer<ILock> {

    public static final String DEFAULT_QUARTZ_LOCK = "defaultQuartzLock";
    public static final String HEARTBEAT_LAST_TIME = "defaultQuartzHeartBeatTime";

    private final IAtomicLong lastHeartBeatTime;
    private final HazelcastInstance hazelcastInstance;

    public QuartzHazelcastInitializer(HazelcastInstance hazelcastInstance, Scheduler scheduler, AppConfig config) {
        super(scheduler, config);
        this.hazelcastInstance = hazelcastInstance;
        this.lastHeartBeatTime = hazelcastInstance.getAtomicLong(HEARTBEAT_LAST_TIME);
    }

    @Override
    protected ILock initLock() {
        return hazelcastInstance.getLock(DEFAULT_QUARTZ_LOCK);
    }

    @Override
    public void unlock() {
        getLock().forceUnlock();
    }

    @Override
    public long getLastHeartbeat() {
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
