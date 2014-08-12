package ru.yandex.qatools.camelot.core;

import org.apache.camel.Exchange;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregationKeyStrategy {

    /**
     * Calculating the aggregation key
     */
    String aggregationKey(Exchange exchange);

}
