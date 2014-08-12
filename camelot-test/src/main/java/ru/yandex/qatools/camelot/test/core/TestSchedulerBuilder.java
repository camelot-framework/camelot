package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.annotations.OnTimer;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AnnotatedMethodListener;
import ru.yandex.qatools.camelot.core.builders.SchedulerBuilder;
import ru.yandex.qatools.camelot.core.impl.AggregatorPluginAnnotatedMethodInvoker;

import java.lang.reflect.Method;

import static ru.yandex.qatools.camelot.util.ReflectUtil.getMethodFromClassHierarchy;
import static ru.yandex.qatools.camelot.util.ServiceUtil.forEachAnnotatedMethod;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestSchedulerBuilder implements SchedulerBuilder {
    final Object pluginMock;
    final SchedulerBuilder original;
    final Plugin plugin;
    final CamelContext camelContext;


    public TestSchedulerBuilder(Object pluginMock, CamelContext camelContext, SchedulerBuilder original, Plugin plugin) {
        this.pluginMock = pluginMock;
        this.original = original;
        this.plugin = plugin;
        this.camelContext = camelContext;
    }

    @Override
    public void schedule() throws Exception {
        original.schedule();
    }

    @Override
    public void unschedule() throws Exception {
        original.unschedule();
    }

    @Override
    public void invokeJobs() throws Exception {
        Class pluginClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        forEachAnnotatedMethod(pluginClass, OnTimer.class, new AnnotatedMethodListener<Object, OnTimer>() {
            @Override
            public Object found(Method method, OnTimer annotation) throws Exception {
                return invokeJob(method.getName());
            }
        });
    }

    @Override
    public boolean invokeJob(String method) throws Exception {
        if (original.invokeJob(method)) {
            AggregatorPluginAnnotatedMethodInvoker invoker =
                    new AggregatorPluginAnnotatedMethodInvoker(camelContext, plugin, OnTimer.class, false);
            invoker.process();
            invoker.setPluginInstance(pluginMock);
            Class pluginClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
            Method m = getMethodFromClassHierarchy(pluginClass, method);
            invoker.invoke(m);
            return true;
        }
        return false;
    }
}
