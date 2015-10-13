package ru.yandex.qatools.camelot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotUrlClassloader extends URLClassLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ClassLoader parent;
    private static final String[] PARENT_PKGS = {
            "ru.yandex.qatools.camelot.api.", "ru.yandex.qatools.fsm.",
            "com.sun.", "org.glassfish", "java.", "javax.", "org.eclipse.jetty.", "org.codehaus", "org.apache.camel",
            "org.apache.activemq", "org.atmosphere", "net.sf.cglib", "ru.yandex.qatools.camelot.core", "ch.lambdaj",
            "com.hazelcast", "org.springframework", "com.fasterxml.jackson", "org.apache.log4j.", "org.slf4j."
    };

    public CamelotUrlClassloader(URL[] urls, ClassLoader parent) {
        super(urls);
        this.parent = parent;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return (isParentUsageRequired(name)) ? parent.loadClass(name) : super.loadClass(name, resolve);
        } catch (Exception ignored) {
            logger.trace("Ignored exception", ignored);
            return super.loadClass(name, resolve);
        }
    }

    private boolean isParentUsageRequired(String name) {
        return !isEmpty(name) && parentPathsContains(name);
    }

    private boolean parentPathsContains(String name) {
        for (String parentPath : PARENT_PKGS) {
            if (name.startsWith(parentPath)) {
                return true;
            }
        }
        return false;
    }
}
