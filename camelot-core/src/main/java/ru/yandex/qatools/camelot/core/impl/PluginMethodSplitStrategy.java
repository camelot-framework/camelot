package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.api.annotations.Split;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.SplitStrategy;

import java.util.List;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginMethodSplitStrategy extends AbstractPluginMethodStrategy<Split> implements SplitStrategy {

    public PluginMethodSplitStrategy(PluginContext pluginContext) {
        super(pluginContext, Split.class);
    }

    @Override
    public List split(Exchange exchange) {
        return (List) dispatchEvent(exchange);
    }
}
