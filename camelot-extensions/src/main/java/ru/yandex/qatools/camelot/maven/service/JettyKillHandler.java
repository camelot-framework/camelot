package ru.yandex.qatools.camelot.maven.service;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ShutdownHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;

import static java.lang.Runtime.getRuntime;

/**
 * @author smecsia
 * @see org.eclipse.jetty.server.handler.ShutdownHandler
 * <p/>
 * A handler that force shuts the server down on a valid request. Used to do "hard" restarts from Java. If _exitJvm ist set
 * to true a hard Runtime.halt() call is being made.
 */
public class JettyKillHandler extends ShutdownHandler {

    private static final Logger LOG = Log.getLogger(ShutdownHandler.class);

    public JettyKillHandler(String shutdownToken, boolean exitJVM, boolean sendShutdownAtStart) {
        super(shutdownToken, exitJVM, sendShutdownAtStart);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        if (!target.equals("/shutdown") && !request.getMethod().equals("POST") && !hasCorrectSecurityToken(request)) {
            super.handle(target, baseRequest, request, response);
            return;
        }

        LOG.info("Shutting down by request from " + request.getRemoteAddr());

        try {
            getServer().stop();
        } catch (Exception e) {
            LOG.warn(e);
        }

        getRuntime().halt(0);
    }

    private boolean hasCorrectSecurityToken(HttpServletRequest request) {
        String tok = request.getParameter("token");
        if (LOG.isDebugEnabled())
            LOG.debug("Token: {}", tok);
        return getShutdownToken().equals(tok);
    }
}
