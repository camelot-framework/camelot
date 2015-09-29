package ru.yandex.qatools.camelot.beans;

import ru.yandex.qatools.camelot.common.AggregationKeyStrategy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AggregatorConfigImpl extends RouteConfigImpl implements AggregatorConfig {
    private AggregationKeyStrategy strategyInstance;

    public AggregatorConfigImpl(RouteConfig routeConfig) {
        this.setFilterInstanceOf(routeConfig.getFilterInstanceOf());
        this.setSplitStrategy(routeConfig.getSplitStrategy());
        this.setCustomFilter(routeConfig.getCustomFilter());
    }

    @Override
    public AggregationKeyStrategy getStrategyInstance() {
        return strategyInstance;
    }

    public void setStrategyInstance(AggregationKeyStrategy strategyInstance) {
        this.strategyInstance = strategyInstance;
    }
}
