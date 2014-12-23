package ru.yandex.qatools.camelot.core.web;

import org.atmosphere.cpr.DefaultBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.Broadcaster;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;

import java.util.concurrent.Future;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 03.07.14
 */
public class PluginBroadcaster extends DefaultBroadcaster implements Broadcaster {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JsonSerializer jsonSerializer;

    protected final Plugin plugin;

    public PluginBroadcaster(Plugin plugin, JsonSerializer jsonSerializer) {
        this.plugin = plugin;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public Future<Object> broadcast(Object msg) {
        try {
            return super.broadcast(msg == null ? null : jsonSerializer.toJson(msg));
        } catch (Exception e) {
            logger.error(String.format("Failed to broadcast the message for plugin '%s'", plugin.getId()), e);
        }
        return null;
    }

    @Override
    public void send(Object message) {
        broadcast(message);
    }
}
