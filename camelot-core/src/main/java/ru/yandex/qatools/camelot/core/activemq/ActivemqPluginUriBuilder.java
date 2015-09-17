package ru.yandex.qatools.camelot.core.activemq;

import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.PluginUriBuilder;
import ru.yandex.qatools.camelot.core.builders.BasicPluginUriBuilder;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class ActivemqPluginUriBuilder extends BasicPluginUriBuilder implements PluginUriBuilder {
    public static final String PLUGIN_IN_PREFIX = "activemq:queue:plugin.input";
    public static final String CLIENT_NOTIFY_URI = "activemq:topic:client.notify";
    public static final String TMP_INPUT_BUFFER_URI = "activemq:queue:tmp.input.buffer";
    public static final String BROADCAST_CONFIG = "?receiveTimeout=15000&requestTimeout=10000" +
            "&destination.consumer.maximumPendingMessageLimit=1&destination.consumer.prefetchSize=1" +
            "&destination.consumer.dispatchAsync=true";

    @Override
    public String broadcastUri(String pluginId, String suffix) {
        return "activemq:topic:" + pluginId + ((isEmpty(suffix)) ? "" : "." + suffix) + BROADCAST_CONFIG;
    }

    @Override
    public String pluginInputUri(Plugin plugin, String suffix, String brokerConfig) {
        return plugin.getBaseInputUri() + "." + plugin.getId()
                + (isEmpty(suffix) ? "" : "." + suffix)
                + (isEmpty(brokerConfig) ? "" : brokerConfig);
    }

    @Override
    public String tmpInputBufferUri() {
        return TMP_INPUT_BUFFER_URI;
    }

    @Override
    public String frontendBroadcastUri() {
        return CLIENT_NOTIFY_URI + BROADCAST_CONFIG;
    }

    @Override
    public String basePluginInputUri() {
        return PLUGIN_IN_PREFIX;
    }
}
