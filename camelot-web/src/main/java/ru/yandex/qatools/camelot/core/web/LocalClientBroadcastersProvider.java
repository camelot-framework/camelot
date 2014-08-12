package ru.yandex.qatools.camelot.core.web;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface LocalClientBroadcastersProvider {
    /**
     * Returns the stored broadcaster for the topic
     * Throws an exception if no broadcasters with such topic found
     */
    PluginSseBroadcaster getBroadcaster(String topic) throws Exception;

    /**
     * Disable this provider (after this it should not provide broadcasters)
     */
    void disable();

    /**
     * Enable this provider
     */
    void enable();
}
