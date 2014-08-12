package ru.yandex.qatools.camelot.core.builders;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryWithHazelcastImpl extends BuildersFactoryImpl {

    final HazelcastInstance hazelcastInstance;
    private long waitForLockSec = MINUTES.toSeconds(5);

    public BuildersFactoryWithHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return new HazelcastAggregationRepositoryBuilder(hazelcastInstance, camelContext, waitForLockSec);
    }

    public long getWaitForLockSec() {
        return waitForLockSec;
    }

    public void setWaitForLockSec(long waitForLockSec) {
        this.waitForLockSec = waitForLockSec;
    }
}
