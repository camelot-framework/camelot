package ru.yandex.qatools.camelot.core;

import java.lang.reflect.Method;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginMethodInvoker {

    void invoke(Method method, Object... args);

    void invoke(Object... args);

}
