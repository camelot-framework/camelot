package ru.yandex.qatools.camelot.core.web;

import org.apache.camel.CamelContext;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.PluginWeb;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static jodd.util.StringUtil.isEmpty;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalClientBroadcastersProviderImpl implements LocalClientBroadcastersProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final Map<String, DummyBroadcaster> dummyBroadcasters = new ConcurrentHashMap<>();

    private boolean enabled = true;
    final CamelContext camelContext;
    final PluginWeb plugin;
    final JsonSerializer jsonSerializer;
    final Map<String, PluginSseBroadcaster> broadcasters = new HashMap<>();

    public LocalClientBroadcastersProviderImpl(CamelContext camelContext, JsonSerializer jsonSerializer, PluginWeb plugin) {
        this.camelContext = camelContext;
        this.plugin = plugin;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public synchronized PluginSseBroadcaster getBroadcaster(String topic) throws Exception {
        try {
            if (!enabled) {
                throw new PluginsSystemException("Failed to retrieve broadcaster: provider is disabled!");
            }
            topic = (isEmpty(topic)) ? "" : topic;
            if (!broadcasters.containsKey(topic)) {
                broadcasters.put(topic, new PluginSseBroadcaster(plugin, jsonSerializer));
            }
            return broadcasters.get(topic);
        } catch (Exception e) {
            logger.warn(String.format("Failed to get the broadcaster for client sender for plugin %s", plugin.getId()), e);
            if (!dummyBroadcasters.containsKey(plugin.getId())) {
                return dummyBroadcasters.put(plugin.getId(), new DummyBroadcaster(plugin, jsonSerializer));
            }
            return dummyBroadcasters.get(plugin.getId());
        }
    }

    @Override
    public void disable() {
        this.enabled = false;
    }

    @Override
    public void enable() {
        this.enabled = true;
    }
}
