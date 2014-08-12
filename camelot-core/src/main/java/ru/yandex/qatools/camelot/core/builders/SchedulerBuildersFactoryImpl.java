package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SchedulerBuildersFactoryImpl implements SchedulerBuildersFactory {
    private final Scheduler quartzScheduler;
    private final CamelContext camelContext;

    public SchedulerBuildersFactoryImpl(Scheduler quartzScheduler, CamelContext camelContext) {
        this.quartzScheduler = quartzScheduler;
        this.camelContext = camelContext;
    }

    @Override
    public SchedulerBuilder build(Plugin plugin) {
        return new QuartzAggregatorSchedulerBuilder(camelContext, quartzScheduler, plugin);
    }
}
