package ru.yandex.qatools.camelot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.ServiceUtil.forEachAnnotatedMethod;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class PluginAnnotatedMethodInvoker<A> implements PluginMethodInvoker {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected final Plugin plugin;
    protected final Class anClass;
    protected List<Method> methods = new ArrayList<>();

    public PluginAnnotatedMethodInvoker(Plugin plugin, Class anClass) {
        this.plugin = plugin;
        this.anClass = anClass;
    }

    public PluginAnnotatedMethodInvoker process() throws Exception { //NOSONAR
        return process(null);
    }

    public PluginAnnotatedMethodInvoker process(final FoundMethodProcessor<A> proc) throws Exception { //NOSONAR
        this.methods = new ArrayList<>();
        Class aggClass = plugin.getContext().getClassLoader().loadClass(plugin.getContext().getPluginClass());
        forEachAnnotatedMethod(aggClass, anClass, new AnnotatedMethodListener<Object, A>() {
            @Override
            public Object found(Method method, A annotation) throws Exception { //NOSONAR
                if (method.getParameterTypes().length <= 2 && (proc == null || proc.appliesTo(method, annotation))) {
                    methods.add(method);
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
        } catch (InvocationTargetException e) {//NOSONAR
            LOGGER.error("Failed to process the plugin's '{}' method '{}' invocation: {}! \n {}",
                    plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e), e.getTargetException());
        } catch (Exception e) {
            LOGGER.error("Failed to process the plugin's '{}' method '{}' invocation: {}! \n {}",
                    plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e), e);
        }
    }
}
