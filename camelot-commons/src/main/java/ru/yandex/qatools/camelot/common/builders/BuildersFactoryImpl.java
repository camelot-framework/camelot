package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.config.Plugin;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryImpl implements BuildersFactory {
    private long waitForLockSec = MINUTES.toSeconds(5);

    @Override
    public AggregatorRoutesBuilder newAggregatorPluginRouteBuilder(CamelContext camelContext,
                                                                   Plugin plugin) throws Exception { //NOSONAR
        return new AggregatorPluginRouteBuilder(camelContext, plugin);
    }

    @Override
    public ProcessorRoutesBuilder newProcessorPluginRouteBuilder(CamelContext camelContext,
                                                                 Plugin plugin) throws Exception { //NOSONAR
        return new ProcessorPluginRouteBuilder(camelContext, plugin);
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception { //NOSONAR
        return new MemoryAggregationRepositoryBuilder(camelContext, waitForLockSec);
    }

    @Override
    public SchedulerBuildersFactory newSchedulerBuildersFactory(Scheduler scheduler, CamelContext camelContext){
        return new SchedulerBuildersFactoryImpl(scheduler, camelContext);
    }

    public long getWaitForLockSec() {
        return waitForLockSec;
    }

    public void setWaitForLockSec(long waitForLockSec) {
        this.waitForLockSec = waitForLockSec;
    }
}
