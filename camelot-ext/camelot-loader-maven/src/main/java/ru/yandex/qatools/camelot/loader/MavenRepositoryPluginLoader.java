package ru.yandex.qatools.camelot.loader;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.clay.aether.Aether;
import ru.qatools.clay.maven.settings.FluentProfileBuilder;
import ru.qatools.clay.maven.settings.FluentRepositoryPolicyBuilder;
import ru.yandex.qatools.camelot.common.CamelotUrlClassloader;
import ru.yandex.qatools.camelot.common.PluginLoader;
import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.util.FileUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.identityHashCode;
import static java.util.Arrays.copyOf;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;
import static org.codehaus.plexus.util.FileUtils.copyFile;
import static org.codehaus.plexus.util.FileUtils.createTempFile;
import static ru.qatools.clay.aether.Aether.MAVEN_CENTRAL_URL;
import static ru.qatools.clay.aether.Aether.aether;
import static ru.qatools.clay.maven.settings.FluentProfileBuilder.newProfile;
import static ru.qatools.clay.maven.settings.FluentRepositoryBuilder.newRepository;
import static ru.qatools.clay.maven.settings.FluentRepositoryPolicyBuilder.newRepositoryPolicy;
import static ru.qatools.clay.maven.settings.FluentSettingsBuilder.newSettings;

/**
 * Plugins loader from the remote maven repository (using Eclipse Aether)
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MavenRepositoryPluginLoader implements PluginLoader {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Entry> classLoadersCache = new ConcurrentHashMap<>();

    private final Aether aether;

    public MavenRepositoryPluginLoader(
            String localRepository, String[] remoteRepositories,
            String updatePolicy, String checksumPolicy) {

        //noinspection ConstantConditions
        remoteRepositories = remoteRepositories != null
                ? copyOf(remoteRepositories, remoteRepositories.length)
                : nullToEmpty(remoteRepositories);

        FluentRepositoryPolicyBuilder policy = newRepositoryPolicy()
                .withChecksumPolicy(checksumPolicy)
                .withUpdatePolicy(updatePolicy);

        FluentProfileBuilder profile = newProfile()
                .withRepository(newRepository()
                        .withUrl(MAVEN_CENTRAL_URL)
                        .withReleases(policy)
                        .withSnapshots(policy));

        int i = 1;
        for (String repository : remoteRepositories) {
            profile.withRepository(newRepository()
                    .withId("repo" + i++)
                    .withUrl(repository)
                    .withReleases(policy)
                    .withSnapshots(policy));
        }

        aether = aether(
                new File(localRepository),
                newSettings().withActiveProfile(profile).build()
        );
    }

    private static class Entry {
        CamelotUrlClassloader classLoader;
        File tempDirectory;

        private Entry(CamelotUrlClassloader classLoader, File tempDirectory) {
            this.classLoader = classLoader;
            this.tempDirectory = tempDirectory;
        }
    }

    /**
     * Generate the id for the loader
     */
    private String id(PluginsSource source) {
        return source.getArtifact() + "-" + identityHashCode(source);
    }

    @Override
    public synchronized ClassLoader getClassLoader(PluginsSource source) throws Exception { //NOSONAR
        final String id = id(source);
        if (!classLoadersCache.containsKey(id)) {
            createClassLoader(source);
        }
        return classLoadersCache.get(id).classLoader;
    }

    @Override
    public synchronized ClassLoader createClassLoader(PluginsSource source) throws Exception { //NOSONAR
        final String id = id(source);
        clearClassLoader(id);

        if (StringUtils.isEmpty(source.getArtifact())) {
            return Thread.currentThread().getContextClassLoader();
        }
        URL[] urls = aether.resolve(source.getArtifact()).getAsUrls();
        List<URL> tempUrls = new ArrayList<>();
        File tempDir = FileUtil.createTempDirectory();
        for (URL url : urls) {
            File origin = new File(url.toURI());
            File temp = createTempFile(origin.getName(), ".jar", tempDir);
            copyFile(origin, temp);
            tempUrls.add(temp.toURI().toURL());
        }

        final CamelotUrlClassloader classLoader = new CamelotUrlClassloader(
                tempUrls.toArray(new URL[tempUrls.size()]),
                Thread.currentThread().getContextClassLoader()
        );
        classLoadersCache.put(id, new Entry(classLoader, tempDir));
        return classLoader;
    }

    @Override
    public InputStream getResourceAsStream(PluginsSource source, String path) throws Exception { //NOSONAR
        return getClassLoader(source).getResourceAsStream(path);
    }

    @Override
    public void releaseClassLoader(PluginsSource source) {
        clearClassLoader(id(source));
    }

    private void clearClassLoader(String id) {
        try {
            if (classLoadersCache.containsKey(id)) {
                final Entry entry = classLoadersCache.get(id);
                entry.classLoader.close();
                deleteDirectory(entry.tempDirectory);
                classLoadersCache.remove(id);
            }
        } catch (Exception e) {
            logger.warn("Failed to clear class loader", e);
        }
    }
}
