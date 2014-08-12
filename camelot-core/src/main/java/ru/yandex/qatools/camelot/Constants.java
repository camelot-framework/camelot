package ru.yandex.qatools.camelot;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public final class Constants {

    public static final String INPUT_QUEUE = "ref:events.input";
    public static final String CLIENT_NOTIFY_URI = "activemq:topic:client.notify";
    public static final String TMP_INPUT_BUFFER_URI = "activemq:queue:tmp.input.buffer";

    public static final String RES_LISTENER_POSTFIX = ".listener";
    public static final String CLIENT_SEND_POSTFIX = ".client";
    public static final String SPLIT_POSTFIX = ".split";
    public static final String FILTERED_POSTFIX = ".filtered";
    public static final String CONSUMER_POSTFIX = ".consumer";
    public static final String PRODUCER_POSTFIX = ".producer";

    public static final String BROADCAST_CONFIG = "?receiveTimeout=15000&requestTimeout=10000" +
            "&destination.consumer.maximumPendingMessageLimit=1&destination.consumer.prefetchSize=1" +
            "&destination.consumer.dispatchAsync=true";

    Constants() {
    }
}
