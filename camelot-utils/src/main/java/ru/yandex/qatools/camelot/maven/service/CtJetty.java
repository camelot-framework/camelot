package ru.yandex.qatools.camelot.maven.service;

import java.io.File;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.07.14
 */
public interface CtJetty {

    public CtJettyClassPath jetty(String webAppDirectory, String contextPath, int port);

    public CtJettyClassPath jetty(File webAppDirectory, String contextPath, int port);

    public void shutdown(int port);
}
