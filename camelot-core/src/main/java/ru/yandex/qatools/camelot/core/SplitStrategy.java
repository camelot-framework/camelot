package ru.yandex.qatools.camelot.core;

import org.apache.camel.Exchange;

import java.util.List;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface SplitStrategy {

    String SPLIT_METHOD_NAME = "split";

    /**
     * Calculating the aggregation key
     */
    List split(Exchange exchange);

}
