package ru.yandex.qatools.camelot.api;

/**
 * The interface for internal plugins interoperability using plugin's class
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginsInteropByClass {
    /**
     * Returns the repository for the plugin by class
     */
    AggregatorRepository repo(Class pluginClass);

    /**
     * Returns the storage for the plugin by class
     */
    Storage storage(Class pluginClass);

    /**
     * Returns the input producer for the plugin by class
     */
    EventProducer input(Class pluginClass);

    /**
     * Returns the output producer for the plugin by class
     */
    EventProducer output(Class pluginClass);

    /**
     * Returns the plugin interop object for the plugin by class
     */
    PluginInterop forPlugin(Class pluginClass);

    /**
     * Returns the plugin client sender
     */
    ClientMessageSender client(Class pluginClass);

    /**
     * Returns the plugin client sender for the topic
     */
    ClientMessageSender client(Class pluginClass, String topic);
}
