package ru.yandex.qatools.camelot.core.builders;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregatorRoutesBuilder extends BasicRoutesBuilder {

    AggregationStrategyBuilder getStrategyBuilder();

    void setStrategyBuilder(AggregationStrategyBuilder strategyBuilder);

}
