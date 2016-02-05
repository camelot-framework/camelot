package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.annotations.OnTimer;
import ru.yandex.qatools.camelot.common.AggregatorPluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.builders.SchedulerBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

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
    public void schedule() throws Exception { //NOSONAR
        original.schedule();
    }

    @Override
    public void unschedule() throws Exception { //NOSONAR
        original.unschedule();
    }

    @Override
    public void invokeJobs() throws Exception { //NOSONAR
        Class pluginClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        forEachAnnotatedMethod(pluginClass, OnTimer.class, (method, annotation) -> invokeJob(method.getName()));
    }

    @Override
    public boolean invokeJob(String method) throws Exception { //NOSONAR
        if (original.invokeJob(method)) {
            Class pluginClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
            final Method m = getMethodFromClassHierarchy(pluginClass, method);
            final OnTimer onTimer = m.getAnnotation(OnTimer.class);
            PluginAnnotatedMethodInvoker invoker;
            if (onTimer.perState()) {
                invoker = new AggregatorPluginAnnotatedMethodInvoker(camelContext, plugin, OnTimer.class, onTimer.readOnly());
            } else {
                invoker = new PluginAnnotatedMethodInvoker(plugin, OnTimer.class);
            }
            invoker.process();
            invoker.setPluginInstance(pluginMock);
            invoker.invoke(m);
            return true;
        }
        return false;
    }
}
