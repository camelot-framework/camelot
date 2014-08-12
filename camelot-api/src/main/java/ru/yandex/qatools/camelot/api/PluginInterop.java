package ru.yandex.qatools.camelot.api;

/**
 * The interface for internal plugin interoperability
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginInterop<T> {
    /**
     * Returns the repository for the plugin
     */
    AggregatorRepository<T> repo();

    /**
     * Returns the storage for the plugin
     */
    Storage<T> storage();

    /**
     * Returns the input producer for the plugin
     */
    EventProducer input();

    /**
     * Returns the output producer for the plugin
     */
    EventProducer output();

    /**
     * Returns the client sender
     */
    ClientMessageSender client();

    /**
     * Returns the plugin client sender for the topic
     */
    ClientMessageSender client(String topic);
}
