package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.Processor;
import ru.yandex.qatools.camelot.beans.AggregatorConfig;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregationStrategyBuilder {

    Processor build() throws Exception; //NOSONAR

    AggregatorConfig getConfig();

    Class<?> getFsmClass();

    Object getFsmBuilder();

    void setFsmBuilder(Object fsmBuilder);

}
