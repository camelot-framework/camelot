package ru.yandex.qatools.camelot.qpid;

import ru.yandex.qatools.camelot.common.PluginUriBuilder;
import ru.yandex.qatools.camelot.common.builders.BasicPluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class QpidPluginUriBuilder extends BasicPluginUriBuilder implements PluginUriBuilder {
    public static final String PLUGIN_IN_PREFIX = "amqp:queue:";
    public static final String CLIENT_NOTIFY_URI = "amqp:topic:frontend.notify;{create:always}";
    public static final String TMP_INPUT_BUFFER_URI = "amqp:queue:tmp.input;{create:always}";

    @Override
    public String pluginUri(Plugin plugin, String suffix, String brokerConfig) {
        return format("%s%s;{create:always}%s",
                plugin.getBaseInputUri(),
                plugin.getId() + (isEmpty(suffix) ? "" : "." + suffix),
                isEmpty(brokerConfig) ? "" : brokerConfig);
    }

    @Override
    public String frontendBroadcastUri() {
        return CLIENT_NOTIFY_URI;
    }

    @Override
    public String basePluginUri() {
        return PLUGIN_IN_PREFIX;
    }
}
