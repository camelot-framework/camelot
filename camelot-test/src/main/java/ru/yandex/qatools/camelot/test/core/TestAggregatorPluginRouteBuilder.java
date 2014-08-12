package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.core.builders.AggregationStrategyBuilder;
import ru.yandex.qatools.camelot.core.builders.AggregatorRoutesBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestAggregatorPluginRouteBuilder implements AggregatorRoutesBuilder {

    final Object fsmMock;
    final AggregatorRoutesBuilder original;

    public TestAggregatorPluginRouteBuilder(Class fsmClass, Object fsmMock, AggregatorRoutesBuilder original) throws Exception {
        this.fsmMock = fsmMock;
        this.original = original;
        original.setStrategyBuilder(new TestAggregationStrategyBuilder(fsmClass, fsmMock, original.getStrategyBuilder()));
    }

    @Override
    public void removeRoutes() throws Exception {
        original.removeRoutes();
    }

    @Override
    public void startRoutes() throws Exception {
        original.startRoutes();
    }

    @Override
    public String toString() {
        return original.toString();
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
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
