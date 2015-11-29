package ru.yandex.qatools.camelot.common;

import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov
 */
public interface PluginUriBuilder {
    /**
     * Generates local plugin uri with id and suffix (without queue)
     */
    String localUri(String pluginId, String suffix);

    /**
     * Generates the plugin uri (with the queue)
     */
    String pluginUri(Plugin plugin, String suffix, String brokerConfig);

    /**
     * Generates the default basic input uri (for all plugins)
     */
    String basePluginUri();

    /**
     * Generates the frontend broadcast uri (for all plugins)
     */
    String frontendBroadcastUri();
}
