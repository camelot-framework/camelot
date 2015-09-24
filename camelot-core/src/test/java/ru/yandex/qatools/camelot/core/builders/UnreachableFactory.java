package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.common.builders.BuildersFactoryImpl;

/**
 * @author Ilya Sadykov
 */
public class UnreachableFactory extends BuildersFactoryImpl {

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception { //NOSONAR
        return new UnreachableAggregationRepositoryBuilder(camelContext, getWaitForLockSec());
    }
}
