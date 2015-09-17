package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Body;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.core.MessagesSerializer;

import static java.util.Arrays.copyOf;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class InstanceOfFilter {
    public static final String FILTER_METHOD_NAME = "filter";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final ClassLoader classLoader;
    final Class[] filterClasses;
    final MessagesSerializer serializer;

    public InstanceOfFilter(ClassLoader classLoader, MessagesSerializer serializer, Class[] filterClasses) {
        this.serializer = serializer;
        this.classLoader = classLoader;
        this.filterClasses = (filterClasses != null) ? copyOf(filterClasses, filterClasses.length) : new Class[0];
    }

    public boolean filter(@Body Object body) throws ClassNotFoundException {
        try {
            final Class bodyClass = classLoader.loadClass(serializer.identifyBodyClassName(body));
            for (Class<?> clazz : filterClasses) {
                Class<?> filterClass = classLoader.loadClass(clazz.getName());
                if (filterClass.isAssignableFrom(bodyClass)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.trace("Failed to apply instanceOf filter", e);
        }
        return false;
    }
}
