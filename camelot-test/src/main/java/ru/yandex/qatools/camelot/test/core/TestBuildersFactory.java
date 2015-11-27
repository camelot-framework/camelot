package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.*;
import ru.yandex.qatools.camelot.config.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ru.yandex.qatools.camelot.test.core.TestUtil.pluginMock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestBuildersFactory extends BuildersFactoryImpl implements QuartzInitializerFactory {
    final BuildersFactory originalBuildersFactory;
    final ApplicationContext applicationContext;
    final Set<QuartzInitializer> quartzInitializers = new HashSet<>();
    Map<String, Object> mocksStorage = new ConcurrentHashMap<>();

    public TestBuildersFactory(BuildersFactory originalBuildersFactory, ApplicationContext applicationContext) {
        this.originalBuildersFactory = originalBuildersFactory;
        this.applicationContext = applicationContext;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception {
        return originalBuildersFactory.newRepositoryBuilder(camelContext);
    }

    @Override
    public AggregatorRoutesBuilder newAggregatorPluginRouteBuilder(CamelContext camelContext,
                                                                   Plugin plugin) throws Exception { //NOSONAR
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        Object mock = pluginMock(plugin);
        mocksStorage.put(plugin.getId(), mock);
        AggregatorRoutesBuilder original = originalBuildersFactory.newAggregatorPluginRouteBuilder(camelContext, plugin);
        return new TestAggregatorPluginRouteBuilder(classToMock, mock, original);
    }

    @Override
    public ProcessorRoutesBuilder newProcessorPluginRouteBuilder(CamelContext camelContext,
                                                                 Plugin plugin) throws Exception { //NOSONAR
        final Class<?> classToMock = plugin.getContext().getClassLoader().loadClass(plugin.getProcessor());
        final Object mock = pluginMock(plugin);
        mocksStorage.put(plugin.getId(), mock);

        ProcessorRoutesBuilder original = originalBuildersFactory.newProcessorPluginRouteBuilder(
                camelContext, plugin
        );

        return new TestProcessorPluginRouteBuilder(
                plugin.getContext().getClassLoader(), classToMock, mock, original, plugin
        );
    }

    @Override
    public SchedulerBuildersFactory newSchedulerBuildersFactory(Scheduler scheduler, CamelContext camelContext) {
        return new TestSchedulerBuildersFactory(super.newSchedulerBuildersFactory(scheduler, camelContext),
                camelContext, mocksStorage);
    }

    @Override
    public QuartzInitializer newQuartzInitilizer(Scheduler scheduler, AppConfig config) {
        final BasicQuartzInitializer initializer = new BasicQuartzInitializer(scheduler, config);
        quartzInitializers.add(initializer);
        return initializer;
    }

    public Map<String, Object> getMocksStorage() {
        return mocksStorage;
    }

    Set<QuartzInitializer> getQuartzInitializers() {
        return quartzInitializers;
    }
}
