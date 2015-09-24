package ru.yandex.qatools.camelot.common;

import ru.yandex.qatools.camelot.config.PluginsSource;

import java.io.InputStream;

/**
 * Interface for plugins loading.
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @see ru.yandex.qatools.camelot.core.impl.MavenRepositoryPluginLoader
 * @see ru.yandex.qatools.camelot.core.impl.SameClasspathPluginLoader
 */
public interface PluginLoader {

    /**
     * Initialize the new classloader for the plugin
     *
     * @param source source for the loader
     */
    ClassLoader createClassLoader(PluginsSource source) throws Exception; //NOSONAR

    /**
     * Returns the new classloader instance or from cache
     *
     * @param source source for the loader
     */
    ClassLoader getClassLoader(PluginsSource source) throws Exception; //NOSONAR

    /**
     * Returns plugin resource input stream or null if not exist
     *
     * @param source source for the loader
     */
    InputStream getResourceAsStream(PluginsSource source, String path) throws Exception; //NOSONAR


    /**
     * Releasing the classloader created for the plugin source
     */
    void releaseClassLoader(PluginsSource source);

}
