package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.core.impl.Metadata.getMeta;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class AbstractPluginMethodStrategy<A extends Annotation> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final PluginContext pluginContext;
    protected final Class pluginClass;
    private final Class<A> annClass;
    private Object defaultResult = null;

    public AbstractPluginMethodStrategy(PluginContext pluginContext, Class<A> methodAnnotation) {
        this.pluginContext = pluginContext;
        this.annClass = methodAnnotation;
        try {
            pluginClass = pluginContext.getClassLoader()
                    .loadClass(pluginContext.getPluginClass());
            if (getMeta(pluginClass).getAnnotatedMethods(methodAnnotation).length < 1) {
                this.defaultResult = defaultReturn();
            }
        } catch (Exception e) {
            throw new PluginsSystemException(format(
                    "Failed to initialize the %s strategy for plugin %s",
                    methodAnnotation.getSimpleName(),
                    pluginContext.getPluginClass()), e);
        }
    }

    protected Object defaultReturn() {
        throw new PluginsSystemException(
                format("Could not find the appropriate @%s method within aggregator %s!",
                        annClass.getSimpleName(), pluginContext.getId()));
    }

    @SuppressWarnings("unchecked")
    protected Object dispatchEvent(Exchange exchange) {
        Object event = null;
        try {
            if (defaultResult != null) {
                return defaultResult;
            }
            pluginContext.getMessagesSerializer().preProcess(exchange, pluginContext.getClassLoader());
            Object aggregator = pluginClass.newInstance();
            pluginContext.getInjector().inject(aggregator, pluginContext, exchange);
            event = exchange.getIn().getBody();
            final Map<Method, Object> res = new AnnotatedMethodDispatcher(
                    aggregator, getMeta(pluginClass)
            ).dispatch(annClass, true, event);
            return res.values().iterator().next();
        } catch (InvocationTargetException e) {
            logger.trace("Sonar trick", e);
            logger.error(format("Failed to dispatch event with plugin %s: %s", pluginClass,
                            formatStackTrace(e.getTargetException())),
                    e.getTargetException());
            throw new PluginsSystemException(format(
                    "Failed to apply strategy %s to the input event %s "
                            + "for plugin %s using one of the methods annotated with %s",
                    getClass().getName(),
                    event == null ? exchange.getIn().getBody() : event,
                    pluginContext.getId(),
                    annClass.getSimpleName()),
                    e.getTargetException());
        } catch (Exception e) {
            logger.error(format("Failed to dispatch event with plugin %s: %s",
                    pluginClass, formatStackTrace(e)), e);
            throw new PluginsSystemException(format(
                    "Failed to apply strategy %s to the input event %s for plugin %s "
                            + "using one of the methods annotated with %s",
                    getClass().getName(),
                    event == null ? exchange.getIn().getBody() : event,
                    pluginContext.getId(),
                    annClass.getSimpleName()), e);
        }
    }
}
