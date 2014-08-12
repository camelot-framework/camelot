package ru.yandex.qatools.camelot.core.web;

import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 03.07.14
 */
public class PluginSseBroadcaster extends SseBroadcaster {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected final JsonSerializer jsonSerializer;

    protected final Plugin plugin;

    public PluginSseBroadcaster(Plugin plugin, JsonSerializer jsonSerializer) {
        this.plugin = plugin;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public void broadcast(OutboundEvent chunk) {
        try {
            super.broadcast(new OutboundEvent.Builder()
                            .id(chunk.getId())
                            .data(jsonSerializer.toJson(chunk.getData()))
                            .mediaType(MediaType.APPLICATION_JSON_TYPE)
                            .comment(chunk.getComment())
                            .build()
            );
        } catch (IOException e) {
            logger.error(String.format(
                    "Failed to broadcast the message for plugin '%s'",
                    plugin.getId()
            ), e);
        }

    }

    public void send(Object object) {
        broadcast(new OutboundEvent.Builder()
                        .data(object)
                        .build()
        );

    }
}
