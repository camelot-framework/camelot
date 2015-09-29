package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.common.FoundMethodProcessor;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginInitializer;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.util.ReflectUtil;

import java.lang.reflect.Method;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class HazelcastPluginInitializer implements PluginInitializer {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final HazelcastInstance hazelcastInstance;

    public HazelcastPluginInitializer(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    public void init(Plugin plugin) throws Exception { //NOSONAR
        new PluginAnnotatedMethodInvoker<>(plugin, OnInit.class).process(new FoundMethodProcessor<Object>() {
            @Override
            public boolean appliesTo(Method method, Object annotation) {
                try {
                    return !(boolean) ReflectUtil.getAnnotationValue(annotation, "synchronous");
                } catch (Exception e) {
                    LOGGER.debug("Failed to read annotation value", e);
                }
                return false;
            }
        }).invoke();

        new HazelcastSyncAnnotatedMethodInvoker<>(hazelcastInstance, plugin, OnInit.class, 30, true).process(
                new FoundMethodProcessor<Object>() {
                    @Override
                    public boolean appliesTo(Method method, Object annotation) {
                        try {
                            return (boolean) ReflectUtil.getAnnotationValue(annotation, "synchronous");
                        } catch (Exception e) {
                            LOGGER.debug("Failed to read annotation value", e);
                        }
                        return false;
                    }
                }).invoke();
    }
}
