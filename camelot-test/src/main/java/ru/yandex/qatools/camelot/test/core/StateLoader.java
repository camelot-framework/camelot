package ru.yandex.qatools.camelot.test.core;

import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.config.PluginContext;

/**
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
class StateLoader {

    private final PluginContext context;

    public StateLoader(PluginContext context) {
        this.context = context;
    }

    public <T> T fetchState(String key) {
        final AggregatorRepository repo = context.getRepository();
        //noinspection unchecked
        return (T) repo.get(key);
    }
}
