package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventSender;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ClientSendersProviderImpl implements ClientSendersProvider {
    final private CamelContext camelContext;
    final private String clientSendUri;

    final Map<String, ClientMessageSender> sendersCache = new ConcurrentHashMap<>();

    public ClientSendersProviderImpl(CamelContext camelContext, String clientSendUri) {
        this.camelContext = camelContext;
        this.clientSendUri = clientSendUri;
    }

    /**
     * Init new client sender without topic (default sender)
     */
    @Override
    public ClientMessageSender getSender() {
        return getSender("");
    }

    /**
     * Init new client sender with topic
     */
    @Override
    public ClientMessageSender getSender(String topic) {
        if (!sendersCache.containsKey(topic)) {
            try {
                sendersCache.put(topic, initEventSender(camelContext, clientSendUri, topic));
            } catch (Exception e) {
                throw new PluginsSystemException("Failed to initialize the client event sender: ", e);
            }
        }
        return sendersCache.get(topic);
    }
}
