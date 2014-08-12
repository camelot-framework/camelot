package ru.yandex.qatools.camelot.api;

/**
 * Interface allowing to send the message to the client
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ClientMessageSender {
    /**
     * Send the new event to the client via websockets
     */
    void send(Object message);

    /**
     * Send the new event to the client via websockets using topic
     */
    void send(String topic, Object message);
}
