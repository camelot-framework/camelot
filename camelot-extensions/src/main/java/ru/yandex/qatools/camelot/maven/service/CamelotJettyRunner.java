package ru.yandex.qatools.camelot.maven.service;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.lang.System.err;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 11.07.14
 *         <p/>
 *         Start Jetty with specified configuration
 */
public class CamelotJettyRunner {

    public static final String SHUTDOWN_PASSWORD = "shutdown-camelot-jetty";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ClassLoader parent;

    private final String[] additionalClassPath;

    /**
     * Create {@link CamelotJettyRunner} with getClass().getClassLoader() parent.
     * Used in {@link CamelotRunner} via reflection api
     */
    @SuppressWarnings("unused")
    public CamelotJettyRunner() {
        parent = getClass().getClassLoader();
        additionalClassPath = new String[]{};
    }

    /**
     * Create {@link CamelotJettyRunner} with new URL class loader with given classpath
     *
     * @param classPath specified classpath for classloader
     */
    public CamelotJettyRunner(String... classPath) {
        this(CamelotJettyRunner.class.getClassLoader(), classPath);
    }

    /**
     * Create {@link CamelotJettyRunner} with specified parent classloader
     *
     * @param parent given parent classloader
     */
    public CamelotJettyRunner(ClassLoader parent, String[] additionalClassPath) {
        this.parent = parent;
        this.additionalClassPath = unify(additionalClassPath);
    }

    /**
     * Start jetty server
     *
     * @param webApp      path to web application directory
     * @param contextPath given context path for jetty server
     * @param port        given port for jetty server
     * @param waitFor     true if need to join to server, false otherwise
     * @throws Exception if can't start server with specified configuration
     */
    public void run(String webApp, String contextPath, int port, boolean waitFor)
            throws Exception {
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        Server server = new Server(port);

        WebAppContext context = new WebAppContext();
        context.setContextPath(contextPath);
        context.setWar(webApp);
        context.setParentLoaderPriority(true);
        context.setClassLoader(createWebAppClassLoader(context));

        server.setHandler(createHandlersForServer(server, context));
        server.start();

        if (waitFor) {
            logger.info("Waiting until process is finished...");
            server.join();
        }
    }

    /**
     * Create {@link WebAppClassLoader} with {@link #parent} as parent class loader and with extra classpath
     *
     * @param context {@link WebAppContext} to set to class loader
     * @return created class loader
     * @throws IOException if can't read resource from one of given extra class path elements
     */
    private WebAppClassLoader createWebAppClassLoader(WebAppContext context) throws IOException {
        WebAppClassLoader classLoader = new WebAppClassLoader(parent, context) {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                try {
                    return super.loadClass(name, resolve);
                } catch (NoClassDefFoundError e) {
                    logger.trace("Ignored exception", e);
                    Class<?> c = super.findClass(name);
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                }
            }
        };
        for (String url : additionalClassPath) {
            classLoader.addClassPath(url);
        }
        return classLoader;
    }

    /**
     * Create {@link org.eclipse.jetty.server.handler.HandlerList} for given jetty server and web application context
     *
     * @param server  specified {@link org.eclipse.jetty.server.Server}
     * @param context specified web application context
     * @return created handlers
     */
    public static HandlerList createHandlersForServer(Server server, WebAppContext context) {
        HandlerList handlers = new HandlerList();
        final ShutdownHandler shutdownHandler = new ShutdownHandler(server, SHUTDOWN_PASSWORD);
        shutdownHandler.setExitJvm(true);
        handlers.setHandlers(new Handler[]{shutdownHandler, context});
        return handlers;
    }

    /**
     * Unify given strings
     *
     * @param strings specified strings to unify
     * @return array of string without duplicates
     */
    public static String[] unify(String... strings) {
        Set<String> result = new LinkedHashSet<>();
        result.addAll(Arrays.asList(strings));
        return result.toArray(new String[result.size()]);
    }

    /**
     * Main point for start Jetty.
     *
     * @param args given arguments.
     *             args[0] - web application directory path
     *             args[1] - web application context path
     *             args[2] - port
     *             args[3] - path to jar with additional classpath
     *             args[4] - true if need to wait for jetty shutdown, false otherwise
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            err.println("Failed to launch Jetty: arguments count must be 6");
            return;
        }

        final String webAppDirectory = args[0];
        final String contextPath = args[1];
        final int port = Integer.parseInt(args[2]);
        final String additionalClassPath = args[3];
        final boolean waitFor = Boolean.parseBoolean(args[4]);

        new CamelotJettyRunner(additionalClassPath).run(webAppDirectory, contextPath, port, waitFor);
    }
}
