package ru.yandex.qatools.camelot.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import ru.yandex.qatools.camelot.config.PluginWeb;
import ru.yandex.qatools.camelot.web.core.WebfrontEngine;
import ru.yandex.qatools.camelot.web.core.AtmosphereClientBroadcastersProvider;
import ru.yandex.qatools.camelot.web.core.PluginBroadcaster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.mock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MockedBroadcastService implements ApplicationListener, ApplicationContextAware {

    final Map<String, Map<String, PluginBroadcaster>> mocks = new ConcurrentHashMap<>();

    @Autowired
    WebfrontEngine pluginsService;

    ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            for (final PluginWeb plugin : pluginsService.getPlugins()) {
                try {
                    plugin.getContext().setLocalBroadcastersProvider(
                            new AtmosphereClientBroadcastersProvider(applicationContext, plugin) {
                                @Override
                                public synchronized PluginBroadcaster getBroadcaster(String topic) {
                                    return getOrAddBroadcaster(plugin.getId(), topic);
                                }
                            });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public PluginBroadcaster getBroadcaster(String pluginId, String topic) {
        return getOrAddBroadcaster(pluginId, topic);
    }

    public PluginBroadcaster getBroadcaster(String pluginId) {
        return getOrAddBroadcaster(pluginId, "");
    }

    private synchronized PluginBroadcaster getOrAddBroadcaster(String pluginId, String topic) {
        if (!mocks.containsKey(pluginId)) {
            mocks.put(pluginId, new HashMap<String, PluginBroadcaster>());
        }
        if (!mocks.get(pluginId).containsKey(topic)) {
            mocks.get(pluginId).put(topic, mock(PluginBroadcaster.class));
        }
        return mocks.get(pluginId).get(topic);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
