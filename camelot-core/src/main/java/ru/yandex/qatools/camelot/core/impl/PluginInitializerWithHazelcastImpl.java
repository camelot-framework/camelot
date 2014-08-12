package ru.yandex.qatools.camelot.core.impl;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.FoundMethodProcessor;

import java.lang.reflect.Method;

import static ru.yandex.qatools.camelot.util.ReflectUtil.getAnnotationValue;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginInitializerWithHazelcastImpl extends PluginInitializerImpl {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HazelcastInstance hazelcastInstance;

    public PluginInitializerWithHazelcastImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void init(Plugin plugin) throws Exception {
        new PluginAnnotatedMethodInvoker<>(plugin, OnInit.class).process(new FoundMethodProcessor<Object>() {
            @Override
            public boolean appliesTo(Method method, Object annotation) {
                try {
                    return !(boolean) getAnnotationValue(annotation, "synchronous");
                } catch (Exception e) {
                    logger.debug("Failed to read annotation value", e);
                }
                return false;
            }
        }).invoke();

        new SynchronizedAnnotatedMethodInvoker<>(hazelcastInstance, plugin, OnInit.class, 30, true).process(
                new FoundMethodProcessor<Object>() {
                    @Override
                    public boolean appliesTo(Method method, Object annotation) {
                        try {
                            return (boolean) getAnnotationValue(annotation, "synchronous");
                        } catch (Exception e) {
                            logger.debug("Failed to read annotation value", e);
                        }
                        return false;
                    }
                }).invoke();
    }
}
