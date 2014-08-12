package ru.yandex.qatools.camelot.beans;

import ru.yandex.qatools.camelot.core.AggregationKeyStrategy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregatorConfig extends RouteConfig {
    AggregationKeyStrategy getStrategyInstance();
}
