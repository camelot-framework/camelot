package ru.yandex.qatools.camelot.common;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.yandex.qatools.camelot.config.PluginWeb;
import ru.yandex.qatools.camelot.core.WebfrontEngine;
import ru.yandex.qatools.camelot.core.web.LocalClientBroadcastersProviderImpl;
import ru.yandex.qatools.camelot.core.web.PluginSseBroadcaster;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MockedBroadcastService implements ApplicationListener, CamelContextAware {

    final Map<String, Map<String, PluginSseBroadcaster>> mocks = new ConcurrentHashMap<>();

    @Autowired
    WebfrontEngine pluginsService;

    @Autowired
    JsonSerializer jsonSerializer;

    CamelContext camelContext;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            for (final PluginWeb plugin : pluginsService.getPlugins()) {
                try {
                    plugin.getContext().setLocalBroadcastersProvider(
                            new LocalClientBroadcastersProviderImpl(camelContext, jsonSerializer, plugin) {
                                @Override
                                public synchronized PluginSseBroadcaster getBroadcaster(String topic) {
                                    return getOrAddBroadcaster(plugin.getId(), topic);
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public PluginSseBroadcaster getBroadcaster(String pluginId, String topic) {
        return getOrAddBroadcaster(pluginId, topic);
    }

    public PluginSseBroadcaster getBroadcaster(String pluginId) {
        return getOrAddBroadcaster(pluginId, "");
    }

    private synchronized PluginSseBroadcaster getOrAddBroadcaster(String pluginId, String topic) {
        if (!mocks.containsKey(pluginId)) {
            mocks.put(pluginId, new HashMap<String, PluginSseBroadcaster>());
        }
        if (!mocks.get(pluginId).containsKey(topic)) {
            mocks.get(pluginId).put(topic, mock(PluginSseBroadcaster.class));
        }
        return mocks.get(pluginId).get(topic);
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
