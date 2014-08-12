package ru.yandex.qatools.camelot.test;

import ru.yandex.qatools.camelot.config.Plugin;

import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface TestHelper {
    void sendTo(Class pluginClass, Object event);

    void sendTo(String pluginId, Object event);

    void sendTo(Class pluginClass, Object event, Map<String, Object> headers);

    void sendTo(Class pluginClass, Object event, String header, Object headerValue);

    void sendTo(String pluginId, Object event, Map<String, Object> headers);

    void sendTo(String pluginId, Object event, String header, Object headerValue);

    void send(Object event);

    void send(Object event, Map<String, Object> headers);

    void send(Object event, String header, Object headerValue);

    void invokeTimersFor(Class pluginClass);

    void invokeTimersFor(String pluginId);

    void invokeTimers(Plugin plugin);
}
