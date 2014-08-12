package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.builders.SchedulerBuilder;
import ru.yandex.qatools.camelot.core.builders.SchedulerBuildersFactory;

import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestSchedulerBuildersFactory implements SchedulerBuildersFactory {
    final private Map<String, Object> mocksStorage;
    final CamelContext camelContext;
    private SchedulerBuildersFactory original;

    public TestSchedulerBuildersFactory(SchedulerBuildersFactory original, CamelContext camelContext, Map<String, Object> mocksStorage) {
        this.original = original;
        this.mocksStorage = mocksStorage;
        this.camelContext = camelContext;
    }

    @Override
    public SchedulerBuilder build(Plugin plugin) {
        return new TestSchedulerBuilder(mocksStorage.get(plugin.getId()), camelContext, original.build(plugin), plugin);
    }
}
