package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.core.impl.Metadata.getMeta;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class AbstractPluginMethodStrategy<A extends Annotation> {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final PluginContext pluginContext;
    protected final Class pluginClass;
    private final Class<A> annClass;
    private Object defaultResult = null;

    protected Object defaultReturn() {
        throw new PluginsSystemException(
                format("Could not find the appropriate @%s method within aggregator %s!",
                        annClass.getSimpleName(), pluginContext.getId()));
    }

    public AbstractPluginMethodStrategy(PluginContext pluginContext, Class<A> methodAnnotation) {
        this.pluginContext = pluginContext;
        this.annClass = methodAnnotation;
        try {
            pluginClass = pluginContext.getClassLoader().loadClass(pluginContext.getPluginClass());
            if (getMeta(pluginClass).getAnnotatedMethods(methodAnnotation).length < 1) {
                this.defaultResult = defaultReturn();
            }
        } catch (Exception e) {
            throw new PluginsSystemException(format("Failed to initialize the %s strategy for plugin %s",
                    methodAnnotation.getSimpleName(), pluginContext.getPluginClass()), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Object dispatchEvent(Exchange exchange) {
        try {
            if (defaultResult != null) {
                return defaultResult;
            }
            final Object event = processInput(exchange.getIn().getBody(), (String) exchange.getIn().getHeader(BODY_CLASS));
            final Object aggregator = pluginClass.newInstance();
            pluginContext.getInjector().inject(aggregator, pluginContext, exchange);
            final AnnotatedMethodDispatcher dispatcher = new AnnotatedMethodDispatcher(aggregator, getMeta(pluginClass));
            Map<Method, Object> res = dispatcher.dispatch(annClass, true, event);
            return res.values().iterator().next();
        } catch (InvocationTargetException e) {
            logger.trace("Sonar trick", e);
            logger.error(format("Failed to dispatch event with plugin %s: %s", pluginClass,
                    formatStackTrace(e.getTargetException())),
                    e.getTargetException());
            throw new PluginsSystemException(format("Failed to apply strategy %s to the input event %s " +
                    "for plugin %s using one of the methods annotated with %s",
                    getClass().getName(), exchange.getIn().getBody(), pluginContext.getId(), annClass.getSimpleName()), e.getTargetException());
        } catch (Exception e) {
            logger.error(format("Failed to dispatch event with plugin %s: %s", pluginClass, formatStackTrace(e)), e);
            throw new PluginsSystemException(format("Failed to apply strategy %s to the input event %s " +
                    "for plugin %s using one of the methods annotated with %s",
                    getClass().getName(), exchange.getIn().getBody(), pluginContext.getId(), annClass.getSimpleName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Object processInput(Object body, String bodyClass) {
        if (body instanceof byte[]) {
            try {
                final ClassLoader classLoader = pluginContext.getClassLoader();
                body = deserializeFromBytes((byte[]) body, classLoader,
                        (Class<? extends Serializable>) classLoader.loadClass(bodyClass));
            } catch (Exception e) {
                logger.debug("Failed to deserialize event object", e);
            }
        }
        return body;
    }
}
