package ru.yandex.qatools.camelot.maven.service;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 08.07.14
 */
public interface CtRun {

    public CtRun jvmArgs(String jvmArgs);

    public CtRun forked(boolean forked);

    public CtRun waitFor(boolean waitFor);

    public void run() throws Exception; //NOSONAR

}
