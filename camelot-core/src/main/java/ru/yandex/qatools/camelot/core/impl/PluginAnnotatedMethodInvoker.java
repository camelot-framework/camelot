package ru.yandex.qatools.camelot.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AnnotatedMethodListener;
import ru.yandex.qatools.camelot.core.FoundMethodProcessor;
import ru.yandex.qatools.camelot.core.PluginMethodInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.ServiceUtil.forEachAnnotatedMethod;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class PluginAnnotatedMethodInvoker<A> implements PluginMethodInvoker {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected List<Method> methods = new ArrayList<>();
    protected final Plugin plugin;
    protected final Class anClass;

    public PluginAnnotatedMethodInvoker(Plugin plugin, Class anClass) {
        this.plugin = plugin;
        this.anClass = anClass;
    }

    public PluginAnnotatedMethodInvoker process() throws Exception {
        return process(null);
    }

    public PluginAnnotatedMethodInvoker process(final FoundMethodProcessor<A> proc) throws Exception {
        this.methods = new ArrayList<>();
        Class aggClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        forEachAnnotatedMethod(aggClass, anClass, new AnnotatedMethodListener<Object, A>() {
            @Override
            public Object found(Method method, A annotation) throws Exception {
                if (method.getParameterTypes().length <= 2) {
                    if (proc == null || proc.appliesTo(method, annotation)) {
                        methods.add(method);
                    }
                }
                return method;
            }
        });
        return this;
    }

    @Override
    public void invoke(Object... args) {
        for (Method method : methods) {
            invoke(method, args);
        }
    }

    @Override
    public void invoke(Method method, Object... args) {
        try {
            Object instance = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass()).newInstance();
            plugin.getContext().getInjector().inject(instance, plugin.getContext());
            method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            logger.trace("Sonar trick", e);
            logger.error(format("Failed to process the plugin's '%s' method '%s' invocation: %s! \n %s",
                    plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e)), e.getTargetException());
        } catch (Exception e) {
            logger.error(format("Failed to process the plugin's '%s' method '%s' invocation: %s! \n %s",
                    plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e)), e);
        }
    }
}
