package ru.yandex.qatools.camelot.maven.service;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ShutdownHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.Runtime.getRuntime;

/**
 * @author smecsia
 */
public class JettyKillHandler extends ShutdownHandler {
    public JettyKillHandler(String shutdownToken, boolean exitJVM, boolean sendShutdownAtStart) {
        super(shutdownToken, exitJVM, sendShutdownAtStart);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        super.handle(target, baseRequest, request, response);
        getRuntime().halt(0);
    }
}
