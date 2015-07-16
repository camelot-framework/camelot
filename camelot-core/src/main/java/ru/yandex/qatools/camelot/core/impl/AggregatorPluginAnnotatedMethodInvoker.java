package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocalKeys;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.lang3.ArrayUtils.addAll;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AggregatorPluginAnnotatedMethodInvoker extends PluginAnnotatedMethodInvoker {
    final CamelContext camelContext;
    final boolean readOnly;
    private Object pluginInstance;

    public AggregatorPluginAnnotatedMethodInvoker(CamelContext camelContext, Plugin plugin,
                                                  Class<? extends Annotation> anClass, boolean readOnly)
            throws ReflectiveOperationException {
        super(plugin, anClass);
        this.camelContext = camelContext;
        this.readOnly = readOnly;
    }

    public void setPluginInstance(Object pluginInstance) {
        this.pluginInstance = pluginInstance;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Method method, Object... args) {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();
        final Set<String> keys = (repo instanceof AggregationRepositoryWithLocalKeys)
                ? ((AggregationRepositoryWithLocalKeys) repo).localKeys() : repo.getKeys();
        logger.debug(format("Invoking method %s of plugin %s, repo class %s", method.getName(), plugin.getId(), repo.getClass().getName()));
        for (String key : keys) {
            Exchange exchange = null;
            try {
                logger.debug(format("Trying to invoke aggregator's method '%s' for key '%s' of plugin %s...", method.getName(), key, plugin.getId()));
                if (repo instanceof AggregationRepositoryWithLocks && readOnly) {
                    exchange = ((AggregationRepositoryWithLocks) repo).getWithoutLock(camelContext, key);
                } else {
                    exchange = repo.get(camelContext, key);
                }
                if (exchange != null) {
                    final ClassLoader classLoader = plugin.getContext().getClassLoader();
                    Object aggregator = (pluginInstance == null) ?
                            classLoader.loadClass(plugin.getAggregator()).newInstance() :
                            pluginInstance;
                    plugin.getContext().getInjector().inject(aggregator, plugin.getContext(), exchange);
                    Object body = exchange.getIn().getBody();
                    Class<? extends Serializable> bodyClass = (Class<? extends Serializable>)
                            classLoader.loadClass((String) exchange.getIn().getHeader(BODY_CLASS));
                    if (body instanceof byte[]) {
                        body = deserializeFromBytes((byte[]) body, classLoader, bodyClass);
                    }
                    Object[] mArgs = new Object[]{body};
                    if (method.getParameterTypes().length > 1) {
                        mArgs = addAll(mArgs, args);
                    }
                    logger.debug(format("Invoking '%s' for key '%s' of plugin %s...", method.getName(), key, plugin.getId()));
                    method.invoke(aggregator, mArgs);
                    logger.debug(format("Invocation of '%s' for key '%s' of plugin %s done successfully", method.getName(), key, plugin.getId()));
                    exchange.getIn().setBody(serializeToBytes(body));
                } else {
                    logger.warn(format("Exchange is already null for plugin %s and key %s", plugin.getId(), key));
                }
            } catch (InvocationTargetException e) {
                logger.trace("Sonar cheat", e);
                logger.error(format("Failed to process the plugin's '%s' method '%s' invocation: %s! \n %s",
                        plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e)), e.getTargetException());
            } catch (Exception e) {
                logger.error(format("Failed to process the plugin's '%s' method '%s' invocation: %s! \n %s",
                        plugin.getId(), method.getName(), e.getMessage(), formatStackTrace(e)), e);
            } finally {
                if (!readOnly) {
                    try {
                        if (exchange != null && exchange.getIn() != null) {
                            repo.add(camelContext, key, exchange);
                        } else {
                            repo.remove(camelContext, key, exchange);
                        }
                    } finally {
                        if (repo instanceof AggregationRepositoryWithLocks) {
                            ((AggregationRepositoryWithLocks) repo).unlockQuietly(key);
                        }
                    }
                }
            }
        }
    }
}
