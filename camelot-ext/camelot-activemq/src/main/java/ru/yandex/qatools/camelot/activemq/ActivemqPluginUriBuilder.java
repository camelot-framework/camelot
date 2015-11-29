package ru.yandex.qatools.camelot.activemq;

import ru.yandex.qatools.camelot.common.PluginUriBuilder;
import ru.yandex.qatools.camelot.common.builders.BasicPluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class ActivemqPluginUriBuilder extends BasicPluginUriBuilder implements PluginUriBuilder {
    public static final String PLUGIN_IN_PREFIX = "activemq:queue:plugin";
    public static final String CLIENT_NOTIFY_URI = "activemq:topic:frontend.notify";
    public static final String BROADCAST_CONFIG = "?receiveTimeout=15000&requestTimeout=10000" +
            "&destination.consumer.maximumPendingMessageLimit=1&destination.consumer.prefetchSize=1" +
            "&destination.consumer.dispatchAsync=true";

    @Override
    public String pluginUri(Plugin plugin, String suffix, String brokerConfig) {
        return plugin.getBaseInputUri() + "." + plugin.getId()
                + (isEmpty(suffix) ? "" : "." + suffix)
                + (isEmpty(brokerConfig) ? "" : brokerConfig);
    }

    @Override
    public String basePluginUri() {
        return PLUGIN_IN_PREFIX;
    }

    @Override
    public String frontendBroadcastUri() {
        return CLIENT_NOTIFY_URI + BROADCAST_CONFIG;
    }
}
