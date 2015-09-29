package ru.yandex.qatools.camelot.maven.service;

import com.gc.iotools.stream.os.OutputStreamToInputStream;
import org.apache.commons.exec.*;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.maven.utils.CamelotRunnerUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.eclipse.jetty.http.HttpMethod.POST;
import static ru.yandex.qatools.camelot.maven.utils.CamelotRunnerUtils.createJarWithClassPath;
import static ru.yandex.qatools.camelot.maven.utils.CamelotRunnerUtils.toUrlArray;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.07.14
 *         <p/>
 *         Using this class you can configure and start camelot application at same or separate fork.
 */
public class CamelotRunner implements CtJetty, CtRun, CtJettyClassPath, CtApplicationClassPath {

    public static final String JETTY_SHUTDOWN_URL_PATTERN = "http://localhost:%d/shutdown?token=%s&_exitJvm=true";
    public static final String LOG4J_IGNORE_TCL = "log4j.ignoreTCL";
    public static final String RUNNER_MERHOD_NAME = "run";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean forked = true;
    private boolean waitFor = true;

    private String[] jettyClassPath;
    private String[] applicationClassPath;

    private String jvmArgs;

    private int port;
    private String webAppDirectory;
    private String contextPath;

    private boolean release = false;

    /**
     * Use {@link #camelot()} for get instance of {@link ru.yandex.qatools.camelot.maven.service.CamelotRunner}
     */
    CamelotRunner() {
    }

    /**
     * A fluent-api method to set {@link #jvmArgs} for separate jvm. Used only in forked mode
     *
     * @param jvmArgs given jvm arguments
     * @return
     */
    @Override
    public CtRun jvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
        return this;
    }

    /**
     * A fluent-api method to set {@link #forked} parameter.
     *
     * @param forked true if you need to start jetty server in separate fork, false otherwise
     */
    @Override
    public CtRun forked(boolean forked) {
        this.forked = forked;
        return this;
    }

    /**
     * A fluent-api method to set {@link #waitFor} parameter.
     *
     * @param waitFor true if you need to wait util server is shutdown, false otherwise
     */
    @Override
    public CtRun waitFor(boolean waitFor) {
        this.waitFor = waitFor;
        return this;
    }

    /**
     * Start Jetty server at http://localhost:{@link #port}/{@link #contextPath}
     *
     * @throws Exception if can't start Jetty server.
     */
    @Override
    public void run() throws Exception { //NOSONAR
        if (forked) {
            runForked();
        } else {
            runNotForked();
        }
    }

    /**
     * A fluent-api method to configure jetty server
     *
     * @param webAppDirectory path to webapp
     * @param contextPath     jetty context path
     * @param port            jetty port
     */
    @Override
    public CtJettyClassPath jetty(String webAppDirectory, String contextPath, int port) {
        this.port = port;
        this.webAppDirectory = webAppDirectory;
        this.contextPath = contextPath;
        return this;
    }

    /**
     * A fluent-api method to configure jetty server
     *
     * @param webAppDirectory {@link java.io.File} with webapp
     * @param contextPath     jetty context path
     * @param port            jetty port
     */
    @Override
    public CtJettyClassPath jetty(File webAppDirectory, String contextPath, int port) {
        jetty(webAppDirectory.getAbsolutePath(), contextPath, port);
        return this;
    }

    /**
     * Send shutdown hook to server at specified port
     *
     * @param port given server port
     */
    @Override
    public void shutdown(int port) {
        try {
            URL url = new URL(String.format(JETTY_SHUTDOWN_URL_PATTERN, port, CamelotJettyRunner.SHUTDOWN_PASSWORD));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(POST.asString());
            final int responseCode = connection.getResponseCode();
            if (responseCode == HttpStatus.OK_200) {
                logger.info(String.format("Server(%d) successfully shutdown", port));
            } else {
                logger.error(String.format("Can't shutdown server(%d). Response code %d", port, responseCode));
            }
        } catch (SocketException e) {
            logger.debug("Server is already not running", e);
        } catch (Exception e) {
            logger.error(String.format("Can't shutdown server(%d)", port), e);
        }
    }


    @Override
    public CtApplicationClassPath jettyClassPath(String... classpath) {
        this.jettyClassPath = classpath;
        return this;
    }

    /**
     * A fluent-api method to set the classpath for application
     *
     * @param cp specified classpath
     */
    @Override
    public CtRun applicationClassPath(String... cp) {
        this.applicationClassPath = cp;
        return this;
    }

    /**
     * Create instance of {@link ru.yandex.qatools.camelot.maven.service.CamelotRunner}
     *
     * @return created instance of camelot runner
     */
    public static CtJetty camelot() {
        return new CamelotRunner();
    }

    /**
     * Run Jetty in separate fork at http://localhost:{@link #port}/{@link #contextPath}. Use {@link #webAppDirectory}
     * as web application context war.
     *
     * @throws Exception if can't start Jetty server.
     */
    protected void runForked() throws Exception { //NOSONAR
        String commandLine = createCommandLine();
        logger.info(String.format("Executing 'java %s'", commandLine));

        CommandLine cmdLine = CommandLine.parse(commandLine);
        Executor executor = CamelotRunnerUtils.createExecutor();

        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        executor.setStreamHandler(createStreamHandler());
        if (waitFor) { // shutdown process if we were killed while waiting for it
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        }
        executor.execute(cmdLine, resultHandler);

        if (waitFor) {
            logger.info("Waiting until process is finished...");
            resultHandler.waitFor();
        } else {
            logger.info("Waiting until camelot is deployed...");
            while (!release) {
                sleep(2000);
            }
            logger.info(String.format(" ==== Camelot is STARTED at http://localhost:%s%s ==== ", port, contextPath));
        }
    }

    /**
     * Create command line string for execute {@link ru.yandex.qatools.camelot.maven.service.CamelotJettyRunner#main(String[])}
     *
     * @return created command line string
     * @throws IOException if can't create temp jars {@link CamelotRunnerUtils#createJarWithClassPath(String, String...)}
     */
    private String createCommandLine() throws IOException {
        logger.info("Jetty Class Path: " + Arrays.toString(jettyClassPath));
        String jettyClassPathJar = createJarWithClassPath(CamelotJettyRunner.class.getName(), jettyClassPath).getAbsolutePath();
        String appClassPathJar = createJarWithClassPath(CamelotJettyRunner.class.getName(), applicationClassPath).getAbsolutePath();

        List<String> params = Arrays.asList("java", jvmArgs, "-jar", jettyClassPathJar,
                webAppDirectory, contextPath, String.valueOf(port), appClassPathJar, String.valueOf(waitFor));

        return StringUtils.join(params, " ");
    }

    /**
     * Create {@link org.apache.commons.exec.PumpStreamHandler} for handle output from child process. Handler
     * looking for {@link CamelotRunnerUtils#WAITFOR_TOKEN}
     *
     * @return created handler
     */
    private PumpStreamHandler createStreamHandler() {
        OutputStreamToInputStream<String> out = new OutputStreamToInputStream<String>() {
            @Override
            protected String doRead(final InputStream is) throws Exception { //NOSONAR
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                //noinspection InfiniteLoopStatement
                while (true) {
                    if (reader.ready()) {
                        final String line = reader.readLine();
                        logger.info(line);
                        if (line.contains(CamelotRunnerUtils.WAITFOR_TOKEN)) {
                            release = true;
                        }
                    }
                    sleep(50);
                }
            }
        };
        return new PumpStreamHandler(out);
    }

    /**
     * Run Jetty in same fork at http://localhost:{@link #port}/{@link #contextPath}. Use {@link #webAppDirectory}
     * as web application context war.
     *
     * @throws Exception if can't start Jetty server.
     */
    protected void runNotForked() throws Exception { //NOSONAR
        System.setProperty(LOG4J_IGNORE_TCL, Boolean.toString(true));

        URLClassLoader classLoader = new URLClassLoader(toUrlArray(jettyClassPath));
        Class<?> clazz = classLoader
                .loadClass(CamelotJettyRunner.class.getName());

        Object obj = clazz.getConstructor(String[].class).newInstance(new Object[]{applicationClassPath});
        clazz.getMethod(RUNNER_MERHOD_NAME, String.class, String.class, int.class, boolean.class)
                .invoke(obj, webAppDirectory, contextPath, port, waitFor);

    }
}
