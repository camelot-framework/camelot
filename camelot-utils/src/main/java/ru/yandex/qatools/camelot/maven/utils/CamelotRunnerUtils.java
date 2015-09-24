package ru.yandex.qatools.camelot.maven.utils;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static jodd.io.FileUtil.createTempFile;
import static org.apache.commons.exec.ExecuteWatchdog.INFINITE_TIMEOUT;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.07.14
 */
public final class CamelotRunnerUtils {

    public static final Logger LOGGER = LoggerFactory.getLogger(CamelotRunnerUtils.class);

    public static final String WAITFOR_TOKEN = "Started ServerConnector";

    public static final String MANIFEST_VERSION = "1.0";

    CamelotRunnerUtils() {
    }

    /**
     * Create Temp Jar file with given classpath in manifest
     *
     * @param mainClassName main class name for manifest
     * @param classPath     classpath elements for manifest
     * @return {@link java.io.File} with created Jar
     * @throws IOException if can't create temp file
     */
    public static File createJarWithClassPath(String mainClassName, String... classPath) throws IOException {
        return createJarWithClassPath(createTempFile(), mainClassName, classPath);
    }

    public static File createJarWithClassPath(File file, String mainClassName, String... classPath) throws IOException {
        JarOutputStream jarOut = new JarOutputStream(
                new FileOutputStream(file),
                createManifest(MANIFEST_VERSION, mainClassName, classPath)
        );
        jarOut.flush();
        jarOut.close();
        return file;
    }

    /**
     * Create manifest with specified version, main class name and classpath.
     *
     * @param version       given version of manifest
     * @param mainClassName given main class name
     * @param classPath     given classpath elements
     * @return created manifest
     */
    public static Manifest createManifest(String version, String mainClassName, String... classPath) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, version);
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClassName);
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, join(classPath, " "));
        return manifest;
    }



    /**
     * Create {@link org.apache.commons.exec.Executor} with infinite timeout watchdog
     *
     * @return created executor
     */
    public static Executor createExecutor() {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(INFINITE_TIMEOUT);
        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        return executor;
    }

    /**
     * Convert given array of strings to URL. If given string is malformed url will return null.
     *
     * @param urls given array to convert
     * @return converted array
     */
    public static URL[] toUrlArray(String[] urls) {
        Set<URL> result = new LinkedHashSet<>();

        for (String url : urls) {
            try {
                result.add(new URL(url));
            } catch (MalformedURLException e) {
                LOGGER.error("Found malformed url in classpath " + url, e);
            }
        }
        return result.toArray(new URL[result.size()]);
    }
}
