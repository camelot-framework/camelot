package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.common.builders.BuildersFactoryImpl;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class HazelcastBuildersFactoryImpl extends BuildersFactoryImpl {

    protected long lockWaitHeartBeatSec = 5;
    protected final HazelcastInstance hazelcastInstance;

    public HazelcastBuildersFactoryImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void setLockWaitHeartBeatSec(long lockWaitHeartBeatSec) {
        this.lockWaitHeartBeatSec = lockWaitHeartBeatSec;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception { //NOSONAR
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, getWaitForLockSec(), lockWaitHeartBeatSec);
    }
}
