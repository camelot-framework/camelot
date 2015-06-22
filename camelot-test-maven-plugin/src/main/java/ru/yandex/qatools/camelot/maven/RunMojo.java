package ru.yandex.qatools.camelot.maven;

import ch.lambdaj.function.convert.Converter;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jodd.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;
import ru.qatools.clay.aether.Aether;
import ru.qatools.clay.aether.AetherException;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginsConfig;
import ru.yandex.qatools.camelot.config.PluginsSource;
import ru.yandex.qatools.camelot.maven.web.ConfigurableWroManagerFactory;
import ru.yandex.qatools.camelot.maven.web.WroFilter;
import ru.qatools.clay.utils.archive.PathJarEntryFilter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static ch.lambdaj.Lambda.convert;
import static java.io.File.separator;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.codehaus.plexus.util.FileUtils.copyFile;
import static ru.yandex.qatools.camelot.maven.service.CamelotRunner.camelot;
import static ru.yandex.qatools.camelot.maven.util.FileUtil.processTemplate;
import static ru.yandex.qatools.camelot.maven.util.FileUtil.replaceInFile;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.ReflectUtil.resolveResourcesAsStringsFromPattern;
import static ru.qatools.clay.aether.Aether.aether;
import static ru.qatools.clay.utils.archive.ArchiveUtil.unpackJar;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.07.14
 */
@SuppressWarnings("JavaDoc unused")
@Mojo(name = "run", requiresDirectInvocation = true, requiresDependencyResolution = ResolutionScope.COMPILE)
public class RunMojo extends AbstractMojo {

    public static final String PLUGIN_PROPERTIES = "/plugin.properties";
    public static final String CAMELOT_PROPERTIES = "/camelot.properties";
    public static final String VERSION_PROPERTY_NAME = "version";
    public static final String CAMELOT_WEB = "camelot-web";
    public static final String CAMELOT_EXTENSIONS = "camelot-extensions";
    public static final String WAR = "war";
    public static final String JAR = "jar";
    public static final String PROPERTIES_FTL = "/plugin.camelot.properties.ftl";
    public static final String WEB_CONTEXT_XML_FTL = "/plugin.camelot-web-context.xml.ftl";

    @Component
    protected MavenProject project;

    @Component
    protected PluginDescriptor plugin;

//    Jetty server configuration

    @Parameter(defaultValue = "8080")
    protected int jettyPort;

    @Parameter(defaultValue = "/camelot")
    protected String contextPath;

    @Parameter(defaultValue = "${project.build.directory}/camelot")
    protected File outputDir;

    @Parameter(property = "camelot-test.runForked", defaultValue = "false")
    protected boolean runForked;

    @Parameter(defaultValue = "-Xmx512m -Xms256m -XX:MaxPermSize=512m")
    protected String jvmArgs;

    @Parameter(defaultValue = "true")
    protected boolean waitUntilFinished;

//    Dependency resolving

    @Component
    protected RepositorySystem system;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession session;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true)
    protected List<RemoteRepository> remotes;

//    Camelot webapp artifact configuration

    @Parameter
    protected String camelotWebArtifact = null;

    @Parameter
    protected String camelotExtensionsArtifact = null;

    @Parameter
    protected String camelotVersion = null;

//    Camelot config

    @Parameter(defaultValue = "${project.build.outputDirectory}/camelot.xml")
    protected File camelotConfigOutputFile;

    @Parameter(defaultValue = "${project.build.outputDirectory}")
    protected String srcOutputDir;

    @Parameter(defaultValue = "${project.build.testOutputDirectory}")
    protected String testOutputDir;

    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    protected ArtifactRepository localRepo;

    @Parameter(defaultValue = "${project.basedir}/src/main/resources")
    protected String srcResDir;

    @Parameter(defaultValue = "${project.basedir}/src/test/resources")
    protected String testSrcResDir;

    @Parameter(defaultValue = "${project.basedir}/src/main/resources/camelot.xml")
    protected File camelotConfigSrcFile;

    @Parameter(defaultValue = "")
    protected String additionalProperties;

    @Parameter(property = "camelot-test.disableMinification", defaultValue = "true")
    private boolean disableMinification;

//    ActiveMQ config

    @Parameter(defaultValue = "true")
    protected boolean useEmbeddedActivemq;

    @Parameter(defaultValue = "tcp://localhost:62618")
    protected String activemqBrokers;

    @Parameter(defaultValue = "62618")
    protected int activemqPort;

    @Parameter(defaultValue = "classpath*:camelot-hz-context.xml")
    protected String hazelcastContextConfigPath;

    @Parameter(property = "camelot-test.inputUri", defaultValue = "activemq:queue:events.input?maxConcurrentConsumers=5")
    protected String mainInputUri;

    @Parameter(property = "camelot-test.outputUri", defaultValue = "direct:plugin.stop")
    protected String mainOutputUri;

//    Fields

    protected Properties properties;

    protected String groupId;

    protected Configuration cfg;

    protected Aether aether;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
            camelotVersion = getCamelotVersion();
            camelotWebArtifact = getCamelotWebArtifact();

            File camelotWeb = aether.resolve(camelotWebArtifact, false).get()
                    .get(0).getArtifact().getFile();

            checkDirectory(outputDir);
            unpackJar(camelotWeb, outputDir);

            removeExtraCamelotConfigFile();

            copyOriginalPropertiesFile();
            File newConfig = createNewPropertiesFile();
            createNewCamelotWebContext(newConfig);

            processWebXmlFile();
            processPluginsScriptsIfNeeded();

            copyCamelotExtensionArtifactToLib();

            camelot().jetty(outputDir, contextPath, jettyPort)
                    .jettyClassPath(createJettyClassPath())
                    .applicationClassPath(createApplicationClassPath())
                    .forked(runForked)
                    .jvmArgs(jvmArgs)
                    .waitFor(waitUntilFinished)
                    .run();
        } catch (Exception e) {
            final String message = "Failed to run camelot";
            getLog().error(message, e);
            throw new MojoFailureException(message, e);
        }
    }

    /**
     * Load plugin properties, extract groupId property and init freemarked configuration.
     *
     * @throws IOException if can't read properties file
     */
    public void init() throws IOException {
        properties = loadPluginProperties();
        groupId = properties.getProperty("groupId");
        cfg = new Configuration();
        cfg.setClassForTemplateLoading(getClass(), "/");
        aether = aether(system, session, remotes);
    }

    public void processPluginsScriptsIfNeeded() throws Exception {
        if (camelotConfigSrcFile.exists()) {
            processMainLayoutFile();
            removePluginsScriptsPattern();
        }
    }

    public void processMainLayoutFile() throws Exception {
        final File mainLayoutFile = new File(outputDir + "/WEB-INF/layouts/main.jade");
        replaceInFile(mainLayoutFile, map(
                " {1,8}script\\(src=\"#\\{context_path\\}/wro/plugins.js\\\"\\)", buildPluginsScripts()
        ));
    }

    public String buildPluginsScripts() throws Exception {
        StringBuilder pluginsScripts = new StringBuilder();
        pluginsScripts.append("        script(src='#{context_path}/wro/plugins.js')\n");
        for (PluginsSource source : loadPluginsConfig().getSources()) {
            for (Plugin plugin : source.getPlugins()) {
                if (StringUtil.isEmpty(plugin.getId())) {
                    plugin.setId(!StringUtil.isEmpty(plugin.getAggregator()) ? plugin.getAggregator() : plugin.getProcessor());
                }
                appendPluginResources(pluginsScripts, plugin, plugin.getResource());
                appendPluginResources(pluginsScripts, plugin, plugin.getAggregator());
                appendPluginResources(pluginsScripts, plugin, plugin.getProcessor());
            }
        }
        return pluginsScripts.toString();
    }

    public void appendPluginResources(StringBuilder pluginsScripts, Plugin plugin, String baseClass) {
        for (String js : findTemplatePaths(baseClass, "**/*", ".js", ".coffee")) {
            final String prePath = (contextPath.endsWith("/")) ? contextPath.substring(0, contextPath.length() - 1) : contextPath;
            pluginsScripts.append("        script(src='").append(prePath).append("/plugin/").
                    append(plugin.getId()).append("/").append(js).append("')\n");
        }
    }

    public List<String> findTemplatePaths(String resClass, String fileBaseName, String... extensions) {
        List<String> paths = new ArrayList<>();
        if (isBlank(resClass)) {
            return Collections.emptyList();
        }
        String basePath = resClass.replaceAll("\\.", separator) + separator;
        for (String ext : extensions) {
            try {
                for (String res : resolveResourcesAsStringsFromPattern("file:" + srcResDir + separator + basePath + fileBaseName + ext)) {
                    paths.add(res.substring(res.indexOf(basePath) + basePath.length()));
                }
            } catch (Exception e) {
                getLog().warn("Failed to find template paths", e);
            }
        }
        return paths;
    }

    /**
     * Loads the plugin system configuration according to the setup
     *
     * @return loaded plugin configuration
     */
    public PluginsConfig loadPluginsConfig() throws Exception {
        JAXBContext jc = JAXBContext.newInstance(PluginsConfig.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return ((PluginsConfig) unmarshaller.unmarshal(camelotConfigSrcFile));
    }

    /**
     * Remove plugins search scripts pattern in wro.xml
     *
     * @throws IOException if an I/O error occurs while reading wro.xml file
     */
    public void removePluginsScriptsPattern() throws IOException {
        File wroXmlFile = new File(outputDir + "/WEB-INF/wro.xml");
        replaceInFile(wroXmlFile, map(
                " {1,8}<js>plugins://\\*\\.js</js>", ""
        ));
    }

    /**
     * Change web.xml file
     *
     * @throws IOException if an I/O error occurs while reading web.xml file
     * @see #disableMinificationIfNeeded(java.io.File)
     * @see #overrideWroConfiguration(java.io.File)
     */
    public void processWebXmlFile() throws IOException {
        File webXmlFile = new File(outputDir + "/WEB-INF/web.xml");
        overrideWroConfiguration(webXmlFile);
        disableMinificationIfNeeded(webXmlFile);
    }

    /**
     * Remove {@code <cssMinJawr>} and {@code <googleClosureSimple>} attributes
     * from web.xml if {@link #disableMinification} is true do nothing otherwise
     *
     * @param webXmlFile web.xml file
     * @throws IOException if an I/O error occurs while reading web.xml file
     */
    public void disableMinificationIfNeeded(File webXmlFile) throws IOException {
        if (disableMinification) {
            replaceInFile(webXmlFile, map(
                    ",cssMinJawr,googleClosureSimple", ""
            ));
        }
    }

    /**
     * Override wro configuration. Change wro filter to {@link ru.yandex.qatools.camelot.core.web.wro.WroFilter} and
     * wro manager factory to {@link ru.yandex.qatools.camelot.core.web.wro.ConfigurableWroManagerFactory}
     *
     * @param webXmlFile web.xml file
     * @throws IOException if an I/O error occurs while reading web.xml file
     */
    public void overrideWroConfiguration(File webXmlFile) throws IOException {
        replaceInFile(webXmlFile, map(
                ru.yandex.qatools.camelot.core.web.wro.WroFilter.class.getName(), WroFilter.class.getName(),
                ru.yandex.qatools.camelot.core.web.wro.ConfigurableWroManagerFactory.class.getName(), ConfigurableWroManagerFactory.class.getName()
        ));
    }

    /**
     * Copy camelot.properties file from camelot jar
     */
    public void copyOriginalPropertiesFile() throws IOException {
        try {
            File originalConfigFile = new File(outputDir + "/WEB-INF/classes/camelot.properties");
            File camelotCoreJar = new File(format("%s/WEB-INF/lib/camelot-core-%s.jar", outputDir, camelotVersion));
            unpackJar(camelotCoreJar, originalConfigFile, new PathJarEntryFilter(CAMELOT_PROPERTIES));
        } catch (IOException | SecurityException e) {
            getLog().error("Can't copy original properties file", e);
        }
    }

    /**
     * Remove duplicate camelot.xml from build output directory
     */
    public void removeExtraCamelotConfigFile() {
        try {
            camelotConfigOutputFile.delete();
        } catch (Exception e) {
            getLog().debug(String.format("Can't delete %s file", camelotConfigOutputFile), e);
        }
    }

    /**
     * Build network connectors for activemq.
     *
     * @return list of connectors
     */
    public List<URI> buildNetworkConnectors() {
        List<URI> connectors = new ArrayList<>();

        for (String broker : activemqBrokers.split(",")) {
            try {
                URI uri = new URI(broker);
                if (uri.getPort() != activemqPort) {
                    connectors.add(uri);
                }
            } catch (URISyntaxException e) {
                getLog().error("Can't parse broker uri", e);
            }
        }
        return connectors;
    }

    /**
     * Create new camelot web context
     *
     * @param newConfigFile new configuration file {@link #createNewPropertiesFile()}
     * @throws IOException       if the file exists but is a directory rather than
     *                           a regular file, does not exist but cannot be created,
     *                           or cannot be opened for any other reason
     * @throws TemplateException if an exception occurs during template processing
     */
    public void createNewCamelotWebContext(File newConfigFile) throws IOException, TemplateException {
        File appContextFile = new File(outputDir + "/WEB-INF/classes/camelot-web-context.xml");
        File originalAppContextFile = new File(outputDir + "/WEB-INF/classes/camelot-web-context.xml.orig.xml");
        File newCamelotWebContextFile = new File(outputDir + "/WEB-INF/classes/camelot-web-context.xml");
        copyFile(appContextFile, originalAppContextFile);
        processTemplate(cfg, WEB_CONTEXT_XML_FTL, new FileWriter(newCamelotWebContextFile), map(
                "useEmbeddedActivemq", useEmbeddedActivemq,
                "originalAppContextFile", originalAppContextFile.getAbsolutePath(),
                "hazelcastContextConfigPath", hazelcastContextConfigPath,
                "activemqBrokers", activemqBrokers,
                "activemqPort", Integer.toString(activemqPort),
                "newConfigFile", newConfigFile.getAbsolutePath(),
                "networkConnectors", buildNetworkConnectors(),
                "additionalProperties", additionalProperties,
                "srcResDir", srcResDir,
                "testSrcResDir", testSrcResDir
        ));
    }

    /**
     * Create new properties file from {@link #PROPERTIES_FTL} template
     *
     * @return {@link java.io.File} new properties file
     * @throws IOException       if the file exists but is a directory rather than
     *                           a regular file, does not exist but cannot be created,
     *                           or cannot be opened for any other reason
     * @throws TemplateException if an exception occurs during template processing
     */
    public File createNewPropertiesFile() throws IOException, TemplateException {
        File newConfigFile = new File(outputDir + "/WEB-INF/classes/new.camelot.properties");
        processTemplate(cfg, PROPERTIES_FTL, new FileWriter(newConfigFile), map(
                "localRepo", localRepo.getBasedir(),
                "remoteRepos", StringUtils.join(convertRemotesToUrls(remotes), ","),
                "activemqBrokers", String.valueOf(activemqBrokers),
                "newConfigFile", newConfigFile.getAbsolutePath(),
                "mainInputUri", mainInputUri,
                "mainOutputUri", mainOutputUri,
                "srcResDir", srcResDir,
                "testSrcResDir", testSrcResDir
        ));
        return newConfigFile;
    }

    /**
     * Convert list of remote repositories to list of strings
     *
     * @param remotes the remotes to convert.
     * @return converted list
     */
    public static List<String> convertRemotesToUrls(List<RemoteRepository> remotes) {
        return convert(remotes, new Converter<RemoteRepository, String>() {
            @Override
            public String convert(RemoteRepository from) {
                return from.getUrl();
            }
        });
    }

    public List<String> convertPathsToUrls(String... strings) {
        return convert(strings, new Converter<String, String>() {
            @Override
            public String convert(String from) {
                return new File(from).toURI().toString();
            }
        });
    }

    public void copyCamelotExtensionArtifactToLib() throws IOException, AetherException {
        File camelotExtensionsJarFile = aether.resolve(
                getCamelotExtensionsArtifact(), false
        ).get().get(0).getArtifact().getFile();

        copyFile(camelotExtensionsJarFile,
                new File(outputDir + "/WEB-INF/lib/" + camelotExtensionsJarFile.getName())
        );
    }

    public String[] createJettyClassPath() throws Exception {
        List<String> classpath = convertPathsToUrls(srcResDir);
        classpath.addAll(getCamelotExtensionsArtifactClassPath());
        return classpath.toArray(new String[classpath.size()]);
    }

    /**
     * Create classpath for application. Contains all compile artifacts from project and camelot-extension artifact
     *
     * @return classpath for application
     * @throws Exception if can't resolve camelot-extensions
     */
    public String[] createApplicationClassPath() throws Exception {
        List<String> classpath = convertPathsToUrls("/WEB-INF/classes", srcOutputDir);
        classpath.addAll(getCompileArtifacts(project.getArtifacts()));
        return classpath.toArray(new String[classpath.size()]);
    }

    /**
     * Get all classpath elements from camelot-extensions artifact
     *
     * @return classpath elements for camelot-extensions
     * @throws Exception if can't resolve camelot-extensions
     */
    public List<String> getCamelotExtensionsArtifactClassPath() throws Exception {
        return Arrays.asList(aether.resolve(getCamelotExtensionsArtifact()).getAsClassPath());
    }

    /**
     * Convert collection of artifacts to classpath
     *
     * @param artifacts to convert
     * @return classpath
     */
    public List<String> getCompileArtifacts(Collection<Artifact> artifacts) {
        List<String> classpath = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            classpath.add(artifact.getFile().toURI().toString());
        }
        return classpath;
    }

    /**
     * Load properties from {@link #PLUGIN_PROPERTIES} file
     *
     * @return created {@link java.util.Properties}
     * @throws IOException if can't read properties file
     */
    public Properties loadPluginProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getResourceAsStream(PLUGIN_PROPERTIES));
        return properties;
    }

    /**
     * Get version of camelot-web artifact.
     *
     * @return version of camelot-web artifact
     */
    public String getCamelotVersion() {
        if (isBlank(camelotVersion)) {
            camelotVersion = properties.getProperty(VERSION_PROPERTY_NAME);
        }
        return camelotVersion;
    }

    /**
     * Get camelot-web artifact coordinates as {groupId}:{artifactId}:{extension}:{version}
     *
     * @return artifact coordinates
     */
    public String getCamelotWebArtifact() {
        if (isBlank(camelotWebArtifact)) {
            camelotWebArtifact = getArtifactCoords(groupId, CAMELOT_WEB, WAR, getCamelotVersion());
        }
        return camelotWebArtifact;
    }

    /**
     * Get camelot-extensions artifact coordinates as {groupId}:{artifactId}:{extension}:{version}
     *
     * @return artifact coordinates
     */
    public String getCamelotExtensionsArtifact() {
        if (isBlank(camelotExtensionsArtifact)) {
            camelotExtensionsArtifact = getArtifactCoords(groupId, CAMELOT_EXTENSIONS, JAR, getCamelotVersion());
        }
        return camelotExtensionsArtifact;
    }

    public String getArtifactCoords(String groupId, String artifactId, String extensions, String version) {
        return String.format("%s:%s:%s:%s", groupId, artifactId, extensions, version);
    }

    /**
     * Check given directory. Try to create directory if doesn't exist.
     *
     * @param dir a directory to check
     * @throws MojoExecutionException if can't create directory
     */
    public void checkDirectory(File dir) throws MojoExecutionException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new MojoExecutionException("Report directory doesn't exists and can't be created.");
        }
    }
}
