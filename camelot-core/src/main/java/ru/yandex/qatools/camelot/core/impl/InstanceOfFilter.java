package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.copyOf;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class InstanceOfFilter {
    public static final String FILTER_METHOD_NAME = "filter";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final ClassLoader classLoader;
    final Class[] filterClasses;

    public InstanceOfFilter(ClassLoader classLoader, Class[] filterClasses) {
        this.classLoader = classLoader;
        this.filterClasses = (filterClasses != null) ? copyOf(filterClasses, filterClasses.length) : new Class[0];
    }

    public boolean filter(@Header(BODY_CLASS) String bodyClassName) throws ClassNotFoundException {
        try {
            Class bodyClass = classLoader.loadClass(bodyClassName);
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
