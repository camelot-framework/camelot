package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;

/**
 * @author Ilya Sadykov
 */
public class UnreachableFactory extends BuildersFactoryImpl {

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return new UnreachableAggregationRepositoryBuilder(camelContext, getWaitForLockSec());
    }
}
