package ru.yandex.qatools.camelot.maven.service;

import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.core.impl.SameClasspathPluginLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginPluginLoader extends SameClasspathPluginLoader {

    final private String testSrcResDir;

    final private String srcResDir;

    public PluginPluginLoader(String testSrcResDir, String srcResDir) {
        this.testSrcResDir = testSrcResDir;
        this.srcResDir = srcResDir;
    }

    private File existingFile(String path) {
        File resFile = new File(testSrcResDir + "/" + path);
        if (resFile.exists()) {
            return resFile;
        }
        resFile = new File(srcResDir + "/" + path);
        if (resFile.exists()) {
            return resFile;
        }
        return null;
    }

    @Override
    public InputStream getResourceAsStream(PluginsSource pluginsSource, String path) throws Exception { //NOSONAR
        try {
            File resFile = existingFile(path);
            if (resFile != null) {
                return new FileInputStream(resFile);
            }
        } catch (IOException e) {
            logger.warn("Failed to get resource as a stream", e);
        }
        return super.getResourceAsStream(pluginsSource, path);
    }
}
