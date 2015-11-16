package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.common.builders.BuildersFactoryImpl;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializer;

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
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception { //NOSONAR
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, getWaitForLockSec(), lockWaitHeartBeatSec);
    }

    @Override
    public QuartzInitializer newQuartzInitializer(Scheduler scheduler, AppConfig config) throws Exception { //NOSONAR
        return new QuartzHazelcastInitializer(hazelcastInstance, scheduler, config);
    }
}
