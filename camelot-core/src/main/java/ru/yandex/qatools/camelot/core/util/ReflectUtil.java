package ru.yandex.qatools.camelot.core.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Util class allowing to scan all the classes inside the specified package and other class operations
 * User: isadykov
 * Date: 16.03.12
 * Time: 15:55
 */
public abstract class ReflectUtil {

    ReflectUtil() {
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of Strings URLs of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<String> resolveResourcesAsStringsFromPattern(final String pattern) //NOSONAR
            throws IOException, ClassNotFoundException {
        return resolveResourcesAsStringsFromPattern(pattern, ReflectUtil.class);
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of Strings URLs of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<String> resolveResourcesAsStringsFromPattern(final String pattern, //NOSONAR
                                                                          final Class<?> baseClass)
            throws IOException, ClassNotFoundException {
        final ClassLoader classLoader = baseClass.getClassLoader();
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        final Collection<String> classes = new LinkedList<>();
        final Resource[] resources = resolver.getResources(pattern);
        for (final Resource resource : resources) {
            final URL url = resource.getURL();
            classes.add(url.toString());
        }
        return classes;
    }


    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern) //NOSONAR
            throws IOException, ClassNotFoundException {
        return resolveResourcesFromPattern(pattern, ReflectUtil.class);
    }


    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern, //NOSONAR
                                                                   final ClassLoader classLoader)
            throws IOException, ClassNotFoundException {
        final Collection<Resource> classes = new LinkedList<>();
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        final Resource[] resources = resolver.getResources(pattern);
        Collections.addAll(classes, resources);
        return classes;
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern, //NOSONAR
                                                                   final Class<?> baseClass)
            throws IOException, ClassNotFoundException {
        return resolveResourcesFromPattern(pattern, baseClass.getClassLoader());
    }
}
