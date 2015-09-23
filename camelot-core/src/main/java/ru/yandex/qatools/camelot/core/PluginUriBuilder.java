package ru.yandex.qatools.camelot.core;

import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov
 */
public interface PluginUriBuilder {
    /**
     * Generates broadcast uri (topic)
     */
    String broadcastUri(String pluginId, String suffix);

    /**
     * Generates local plugin uri with id and suffix
     */
    String localUri(String pluginId, String suffix);

    /**
     * Generates the plugin input uri
     */
    String pluginInputUri(Plugin plugin, String suffix, String brokerConfig);

    /**
     * Generates uri for tmp buffer
     */
    String tmpInputBufferUri();

    /**
     * Generates the frontend broadcast uri (for all plugins)
     */
    String frontendBroadcastUri();

    /**
     * Generates the default basic input uri (for all plugins)
     */
    String basePluginInputUri();
}
