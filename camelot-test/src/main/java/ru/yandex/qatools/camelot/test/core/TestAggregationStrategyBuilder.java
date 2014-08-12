package ru.yandex.qatools.camelot.test.core;

import ru.yandex.qatools.camelot.beans.AggregatorConfig;
import ru.yandex.qatools.camelot.core.builders.AggregationStrategyBuilder;
import ru.yandex.qatools.camelot.core.impl.CamelotAggregationStrategy;
import ru.yandex.qatools.camelot.core.impl.CamelotFSMBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestAggregationStrategyBuilder implements AggregationStrategyBuilder {
    final Object fsmMock;
    final AggregationStrategyBuilder original;

    @SuppressWarnings("unchecked")
    TestAggregationStrategyBuilder(Class fsmClass, Object fsmMock, AggregationStrategyBuilder original) {
        this.fsmMock = fsmMock;
        this.original = original;
        original.setFsmBuilder(
                new TestFSMEngineBuilder(fsmClass, fsmMock, (CamelotFSMBuilder) original.getFsmBuilder())
        );
    }

    @Override
    public AggregatorConfig getConfig() {
        return original.getConfig();
    }

    @Override
    public Class<?> getFsmClass() {
        return original.getFsmClass();
    }

    @Override
    public Object getFsmBuilder() {
        return original.getFsmBuilder();
    }

    @Override
    public void setFsmBuilder(Object fsmBuilder) {
        original.setFsmBuilder(fsmBuilder);
    }

    @Override
    public CamelotAggregationStrategy build() throws Exception {
        return original.build();
    }
}
