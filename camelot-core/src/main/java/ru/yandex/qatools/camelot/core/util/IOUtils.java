package ru.yandex.qatools.camelot.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xeustechnologies.jcl.JarClassLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class IOUtils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    IOUtils() {
    }

    /**
     * Convert the url to inputstream assuming it is a file or resource within jar
     */
    public static InputStream readResource(URL url) throws URISyntaxException, FileNotFoundException { //NOSONAR
        if (!url.toURI().isOpaque() || url.toString().startsWith("file:")) {
            LOGGER.info(String.format("Config file path is trivial: %s", url));
            return new FileInputStream(new File(url.getFile()));
        } else if (url.toString().startsWith("jar:")) {
            LOGGER.info(String.format("Config file path is inside jar: %s", url));
            try {
                JarURLConnection urlConnection = (JarURLConnection) url.openConnection();
                JarClassLoader loader = new JarClassLoader();
                loader.add(urlConnection.getJarFileURL());
                loader.getLocalLoader().setOrder(1);
                loader.getCurrentLoader().setEnabled(false);
                return loader.getResourceAsStream(urlConnection.getEntryName());
            } catch (Exception e) {
                LOGGER.error(String.format("Failed to load the configuration from jar file %s", url), e);
            }
        }
        return null;
    }
}
