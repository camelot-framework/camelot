package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryWithHazelcastImpl extends BuildersFactoryImpl {

    private final HazelcastInstance hazelcastInstance;

    public BuildersFactoryWithHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, getWaitForLockSec());
    }

    @Override
    public QuartzInitializer newQuartzInitializer(Scheduler scheduler, AppConfig config) throws Exception {
        return new QuartzHazelcastInitializerImpl(hazelcastInstance, scheduler, config);
    }
}
