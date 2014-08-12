package ru.yandex.qatools.camelot.maven.service;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 15.07.14
 */
public interface CtJettyClassPath {

    CtApplicationClassPath jettyClassPath(String... classpath);
}
