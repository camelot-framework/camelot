package ru.yandex.qatools.camelot.test.service;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.common.ProcessingEngine;
import ru.yandex.qatools.camelot.config.Plugin;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public class MockedClientSenderInitializer implements CamelContextAware {
    final ProcessingEngine service;
    Map<String, Provider> clientSenders = new ConcurrentHashMap<>();

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

    @Override
    public CamelContext getCamelContext() {
        return null;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(service.getUriBuilder().frontendBroadcastUri()).stop();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to add new routes to test context", e);//NOSONAR
        }
    }

    public Map<String, Provider> getClientSenders() {
        return clientSenders;
    }

    public static class Provider implements ClientSendersProvider {
        final String pluginId;
        Map<String, ClientMessageSender> clientSenders = new ConcurrentHashMap<>();

        private Provider(String pluginId) {
            this.pluginId = pluginId;
        }

        @Override
        public synchronized ClientMessageSender getSender(String topic, String pluginId, String feUrl) {
            if (!clientSenders.containsKey(topic)) {
                clientSenders.put(topic, mock(ClientMessageSender.class));
            }
            return clientSenders.get(topic);
        }

        public Map<String, ClientMessageSender> getClientSenders() {
            return clientSenders;
        }
    }
}
