package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface BuildersFactory {
    /**
     * Initializes the new aggregator plugin route builder
     */
    AggregatorRoutesBuilder newAggregatorPluginRouteBuilder(CamelContext camelContext,
                                                            Plugin plugin) throws Exception;
    /**
     * Initializes the new processor plugin route builder
     */
    ProcessorRoutesBuilder newProcessorPluginRouteBuilder(CamelContext camelContext, Plugin plugin) throws Exception;

    /**
     * Initializes the repository builder
     */
    AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception;

    /**
     * Initializes the quartz initializer
     */
    QuartzInitializer newQuartzInitializer(Scheduler scheduler, AppConfig config) throws Exception;

    /**
     * Initializes the scheduler builders factory
     */
    SchedulerBuildersFactory newSchedulerBuildersFactory(Scheduler scheduler, CamelContext camelContext);

}
