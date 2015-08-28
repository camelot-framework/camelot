package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.error.RepositoryDirtyWriteAttemptException;
import ru.yandex.qatools.camelot.api.error.RepositoryLockWaitException;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocalKeys;
import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocks;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static org.apache.commons.lang3.ArrayUtils.addAll;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
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
    public void invoke(Method method, Object... args) {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();
        final Set<String> keys = (repo instanceof AggregationRepositoryWithLocalKeys)
                ? ((AggregationRepositoryWithLocalKeys) repo).localKeys() : repo.getKeys();

        logger.debug("Invoking method {} of plugin {}, repo class {}",
                method.getName(), plugin.getId(), repo.getClass().getName());

        for (String key : keys) {
            invokeForKey(repo, key, method, args);
        }
    }

    private void invokeForKey(AggregationRepository repo, String key, Method method, Object[] args) {
        try {
            invokeForKeyOrDie(repo, key, method, args);
        } catch (RepositoryLockWaitException e) {
            logger.warn("Failed to process the plugin's '{}' method '{}' invocation: {}!",
                    plugin.getId(), method.getName(), e.getMessage());
            repo.confirm(camelContext, key);
        } catch (RepositoryUnreachableException | RepositoryDirtyWriteAttemptException e) {
            logger.warn("Failed to process the plugin's '{}' method '{}' invocation: {}!",
                    plugin.getId(), method.getName(), e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error("Failed to process the plugin's '{}' method '{}' invocation!",
                    plugin.getId(), method.getName(), e.getTargetException());
        } catch (Exception e) {
            logger.error("Failed to process the plugin's '{}' method '{}' invocation!",
                    plugin.getId(), method.getName(), e);
        } finally {
            unlockQuietly(repo, key);
        }
    }

    private void invokeForKeyOrDie(AggregationRepository repo, String key, Method method, Object[] args) throws Exception {
        logger.debug("Trying to invoke aggregator's method '{}' for key '{}' of plugin {}",
                method.getName(), key, plugin.getId());

        Exchange exchange = getExchange(repo, key);
        if (exchange != null) {
            invokeForExchange(key, exchange, method, args);
            if (!readOnly) {
                repo.add(camelContext, key, exchange);
            }
        } else {
            logger.warn("Exchange is already null for plugin {} and key {}", plugin.getId(), key);
        }
    }

    private Exchange getExchange(AggregationRepository repo, String key) {
        Exchange exchange;
        if (readOnly && repo instanceof AggregationRepositoryWithLocks) {
            exchange = ((AggregationRepositoryWithLocks) repo).getWithoutLock(camelContext, key);
        } else {
            exchange = repo.get(camelContext, key);
        }
        return exchange;
    }

    @SuppressWarnings("unchecked")
    private void invokeForExchange(String key, Exchange exchange, Method method, Object[] args) throws Exception {
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

        logger.debug("Invoking '{}' for key '{}' of plugin {}...",
                method.getName(), key, plugin.getId());
        method.invoke(aggregator, mArgs);
        logger.debug("Invocation of '{}' for key '{}' of plugin {} done successfully",
                method.getName(), key, plugin.getId());

        exchange.getIn().setBody(serializeToBytes(body));
    }

    private void unlockQuietly(AggregationRepository repo, String key) {
        if (!readOnly && repo instanceof AggregationRepositoryWithLocks) {
            ((AggregationRepositoryWithLocks) repo).unlockQuietly(key);
        }
    }
}
