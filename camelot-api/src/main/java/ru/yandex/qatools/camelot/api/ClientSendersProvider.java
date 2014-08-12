package ru.yandex.qatools.camelot.api;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ClientSendersProvider {
    /**
     * Initializes or gets the client sender for the topic
     */
    ClientMessageSender getSender(String topic);

    /**
     * Initializes or gets the client sender for the empty topic
     */
    ClientMessageSender getSender();
}
