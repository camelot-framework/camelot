package ru.yandex.qatools.camelot.common;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;

import java.util.Map;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface AggregationRepositoryWithValuesMap {

    /**
     * Returns a map of all keys to the corresponding states
     */
    Map<String, Exchange> values(CamelContext camelContext);
}
