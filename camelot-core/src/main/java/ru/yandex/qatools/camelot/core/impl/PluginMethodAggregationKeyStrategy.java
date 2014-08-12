package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.api.Constants;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.AggregationKeyStrategy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginMethodAggregationKeyStrategy extends AbstractPluginMethodStrategy<AggregationKey> implements AggregationKeyStrategy {

    public PluginMethodAggregationKeyStrategy(PluginContext pluginContext) {
        super(pluginContext, AggregationKey.class);
    }

    @Override
    public String aggregationKey(Exchange exchange) {
        return (String) dispatchEvent(exchange);
    }

    @Override
    protected Object defaultReturn() {
        return Constants.Keys.ALL;
    }
}
