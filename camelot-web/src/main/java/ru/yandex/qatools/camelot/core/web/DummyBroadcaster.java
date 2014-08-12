package ru.yandex.qatools.camelot.core.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.web.jackson.JsonSerializer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class DummyBroadcaster extends PluginSseBroadcaster {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public DummyBroadcaster(Plugin plugin, JsonSerializer jsonSerializer) {
        super(plugin, jsonSerializer);
    }

    @Override
    public void send(Object message) {
        logger.warn(String.format(
                "Dropping client message from '%s' " +
                        "due to delayed web context initialization: %s",
                plugin.getId(), message));
    }
}
