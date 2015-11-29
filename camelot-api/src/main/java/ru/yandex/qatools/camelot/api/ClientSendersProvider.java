package ru.yandex.qatools.camelot.api;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ClientSendersProvider {
    /**
     * Initializes or gets the client sender for the topic
     */
    ClientMessageSender getSender(String topic, String pluginId, String feSendUri);

    /**
     * Initializes or gets the client sender for the empty topic
     */
    default ClientMessageSender getSender(String pluginId, String feSendUri){
        return getSender("", pluginId, feSendUri);
    }
}
