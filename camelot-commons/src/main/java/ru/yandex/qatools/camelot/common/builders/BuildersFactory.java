package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface BuildersFactory {
    /**
     * Initializes the new aggregator plugin route builder
     */
    AggregatorRoutesBuilder newAggregatorPluginRouteBuilder(CamelContext camelContext,
                                                            Plugin plugin) throws Exception; //NOSONAR
    /**
     * Initializes the new processor plugin route builder
     */
    ProcessorRoutesBuilder newProcessorPluginRouteBuilder(CamelContext camelContext, Plugin plugin) throws Exception; //NOSONAR

    /**
     * Initializes the repository builder
     */
    AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception; //NOSONAR

    /**
     * Initializes the scheduler builders factory
     */
    SchedulerBuildersFactory newSchedulerBuildersFactory(Scheduler scheduler, CamelContext camelContext);

}
