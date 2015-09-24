package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.common.builders.AggregationStrategyBuilder;
import ru.yandex.qatools.camelot.common.builders.AggregatorRoutesBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestAggregatorPluginRouteBuilder implements AggregatorRoutesBuilder {

    final Object fsmMock;
    final AggregatorRoutesBuilder original;

    public TestAggregatorPluginRouteBuilder(Class fsmClass, Object fsmMock,
                                            AggregatorRoutesBuilder original) throws Exception { //NOSONAR
        this.fsmMock = fsmMock;
        this.original = original;
        original.setStrategyBuilder(new TestAggregationStrategyBuilder(fsmClass, fsmMock, original.getStrategyBuilder()));
    }

    @Override
    public void removeRoutes() throws Exception { //NOSONAR
        original.removeRoutes();
    }

    @Override
    public void startRoutes() throws Exception { //NOSONAR
        original.startRoutes();
    }

    @Override
    public String toString() {
        return original.toString();
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception { //NOSONAR
        original.addRoutesToCamelContext(context);
    }

    @Override
    public AggregationStrategyBuilder getStrategyBuilder() {
        return original.getStrategyBuilder();
    }

    @Override
    public void setStrategyBuilder(AggregationStrategyBuilder strategyBuilder) {
        original.setStrategyBuilder(strategyBuilder);
    }
}
