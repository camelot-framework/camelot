package ru.yandex.qatools.camelot.test.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.ProcessingEngine;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public class MockedClientSenderInitializer {
    Map<String, Provider> clientSenders = new ConcurrentHashMap<>();
    final ProcessingEngine service;

    @Autowired
    MockedClientSenderInitializer(ProcessingEngine service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        for (Plugin plugin : service.getPluginsMap().values()) {
            final Provider provider = new Provider(plugin.getId());
            clientSenders.put(plugin.getId(), provider);
            plugin.getContext().setClientSendersProvider(provider);
        }
    }

    public static class Provider implements ClientSendersProvider {
        final String pluginId;
        Map<String, ClientMessageSender> clientSenders = new ConcurrentHashMap<>();

        private Provider(String pluginId) {
            this.pluginId = pluginId;
        }

        @Override
        public synchronized ClientMessageSender getSender(String topic) {
            if (!clientSenders.containsKey(topic)) {
                clientSenders.put(topic, mock(ClientMessageSender.class));
            }
            return clientSenders.get(topic);
        }

        @Override
        public ClientMessageSender getSender() {
            return getSender("");
        }

        public Map<String, ClientMessageSender> getClientSenders() {
            return clientSenders;
        }
    }

    public Map<String, Provider> getClientSenders() {
        return clientSenders;
    }
}
