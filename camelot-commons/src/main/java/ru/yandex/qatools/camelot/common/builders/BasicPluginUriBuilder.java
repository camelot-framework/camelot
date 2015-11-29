package ru.yandex.qatools.camelot.common.builders;

import ru.yandex.qatools.camelot.common.PluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class BasicPluginUriBuilder implements PluginUriBuilder {

    public static final String DIRECT_PREFIX = "direct:";

    @Override
    public String localUri(String pluginId, String suffix) {
        return DIRECT_PREFIX + "plugin." + pluginId + (isEmpty(suffix) ? "" : "." + suffix);
    }

    @Override
    public String pluginUri(Plugin plugin, String suffix, String brokerConfig) {
        return plugin.getBaseInputUri() + "." + plugin.getId() +
                (isEmpty(suffix) ? "" : "." + suffix) +
                (isEmpty(brokerConfig) ? "" : brokerConfig);
    }

    @Override
    public String basePluginUri() {
        return "seda:plugin";
    }

    @Override
    public String frontendBroadcastUri() {
        return DIRECT_PREFIX + "frontend.notify";
    }
}
