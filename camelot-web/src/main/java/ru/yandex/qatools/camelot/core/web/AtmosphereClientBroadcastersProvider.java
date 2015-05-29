package ru.yandex.qatools.camelot.core.web;

import org.atmosphere.cpr.AtmosphereFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.qatools.camelot.config.PluginWeb;
import ru.yandex.qatools.camelot.core.Broadcaster;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static jodd.util.StringUtil.isEmpty;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AtmosphereClientBroadcastersProvider implements LocalClientBroadcastersProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public static final Map<String, DummyBroadcaster> dummyBroadcasters = new ConcurrentHashMap<>();

    private boolean enabled = true;
    final PluginWeb plugin;
    final JsonSerializer jsonSerializer;
    final ApplicationContext appContext;
    final Map<String, Broadcaster> broadcasters = new HashMap<>();

    public AtmosphereClientBroadcastersProvider(ApplicationContext applicationContext, PluginWeb plugin) {
        this.plugin = plugin;
        this.jsonSerializer = applicationContext.getBean(JsonSerializer.class);
        this.appContext = applicationContext;
    }

    @Override
    public synchronized Broadcaster getBroadcaster(String topic) throws Exception {
        try {
            if (!enabled) {
                throw new PluginsSystemException("Failed to retrieve broadcaster: provider is disabled!");
            }
            topic = (isEmpty(topic)) ? "" : topic;
            if (!broadcasters.containsKey(topic)) {
                final PluginBroadcaster broadcaster = initBroadcaster(format("%s/%s", plugin.getId(), topic));
                broadcasters.put(topic, broadcaster);
            }
            return broadcasters.get(topic);
        } catch (Exception e) {
            logger.warn(format("Failed to get the client sender broadcaster for plugin %s", plugin.getId()), e);
            if (!dummyBroadcasters.containsKey(plugin.getId())) {
                return dummyBroadcasters.put(plugin.getId(), new DummyBroadcaster(plugin, jsonSerializer));
            }
            return dummyBroadcasters.get(plugin.getId());
        }
    }

    private PluginBroadcaster initBroadcaster(String broadcasterId) throws UnsupportedEncodingException {
        AtmosphereFramework framework = null;
        if (appContext instanceof WebApplicationContext) {
            framework = (AtmosphereFramework) ((WebApplicationContext) appContext).getServletContext().
                    getAttribute(AtmosphereServlet.FRAMEWORK_ATTR);
        }
        if (framework == null) {
            throw new RuntimeException("Failed to init broadcaster: application context is probably not web application context!");
        }
        final PluginBroadcaster broadcaster = new PluginBroadcaster(plugin, jsonSerializer);
        broadcaster.initialize(broadcasterId, URI.create(encode("http://localhost/" + broadcasterId, "UTF-8")), framework.getAtmosphereConfig());
        return broadcaster;
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
