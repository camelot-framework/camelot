package ru.yandex.qatools.camelot.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.core.PluginLoader;

import java.io.InputStream;

/**
 * Plugins loader from the same classpath
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SameClasspathPluginLoader implements PluginLoader {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ClassLoader createClassLoader(PluginsSource source) throws Exception {
        return source.getClass().getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader(PluginsSource source) throws Exception {
        return createClassLoader(source);
    }

    @Override
    public InputStream getResourceAsStream(PluginsSource source, String path) throws Exception {
        return getClassLoader(source).getResourceAsStream(path);
    }

    @Override
    public void releaseClassLoader(PluginsSource source) {
        // Nothing to do
    }
}
