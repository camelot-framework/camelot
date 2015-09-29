package ru.yandex.qatools.camelot.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.common.PluginLoader;
import ru.yandex.qatools.camelot.config.PluginsSource;

import java.io.InputStream;

/**
 * Plugins loader from the same classpath
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SameClasspathPluginLoader implements PluginLoader {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ClassLoader createClassLoader(PluginsSource source) throws Exception { //NOSONAR
        return source.getClass().getClassLoader();
    }

    @Override
    public ClassLoader getClassLoader(PluginsSource source) throws Exception { //NOSONAR
        return createClassLoader(source);
    }

    @Override
    public InputStream getResourceAsStream(PluginsSource source, String path) throws Exception { //NOSONAR
        return getClassLoader(source).getResourceAsStream(path);
    }

    @Override
    public void releaseClassLoader(PluginsSource source) {
        // Nothing to do
    }
}
