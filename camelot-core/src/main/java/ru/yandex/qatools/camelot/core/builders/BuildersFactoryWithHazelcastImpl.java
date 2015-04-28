package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryWithHazelcastImpl extends BuildersFactoryImpl {

    final HazelcastInstance hazelcastInstance;

    public BuildersFactoryWithHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, getWaitForLockSec());
    }
}
