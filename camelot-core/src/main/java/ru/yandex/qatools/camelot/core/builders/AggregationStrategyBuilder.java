package ru.yandex.qatools.camelot.core.builders;

import ru.yandex.qatools.camelot.beans.AggregatorConfig;
import ru.yandex.qatools.camelot.core.impl.CamelotAggregationStrategy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregationStrategyBuilder {

    CamelotAggregationStrategy build() throws Exception;

    AggregatorConfig getConfig();

    Class<?> getFsmClass();

    Object getFsmBuilder();

    void setFsmBuilder(Object fsmBuilder);

}
