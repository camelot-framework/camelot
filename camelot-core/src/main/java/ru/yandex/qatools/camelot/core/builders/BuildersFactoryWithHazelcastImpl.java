package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryWithHazelcastImpl extends BuildersFactoryImpl {

    protected long lockWaitHeartBeatSec = 5;
    protected final HazelcastInstance hazelcastInstance;

    public BuildersFactoryWithHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void setLockWaitHeartBeatSec(long lockWaitHeartBeatSec) {
        this.lockWaitHeartBeatSec = lockWaitHeartBeatSec;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, getWaitForLockSec(), lockWaitHeartBeatSec);
    }

    @Override
    public QuartzInitializer newQuartzInitializer(Scheduler scheduler, AppConfig config) throws Exception {
        return new QuartzHazelcastInitializerImpl(hazelcastInstance, scheduler, config);
    }
}
