package ru.yandex.qatools.camelot.api;

/**
 * The interface for internal plugins interoperability
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginsInteropById {
    /**
     * Returns the repository for the plugin by id
     */
    AggregatorRepository repo(String pluginId);

    /**
     * Returns the storage for the plugin by id
     */
    Storage storage(String pluginId);

    /**
     * Returns the input producer for the plugin by id
     */
    EventProducer input(String pluginId);

    /**
     * Returns the output producer for the plugin by id
     */
    EventProducer output(String pluginId);

    /**
     * Returns the plugin interop object for the plugin by id
     */
    PluginInterop forPlugin(String pluginId);

    /**
     * Returns the plugin client sender
     */
    ClientMessageSender client(String pluginId);

    /**
     * Returns the plugin client sender for the topic
     */
    ClientMessageSender client(String pluginId, String topic);
}
