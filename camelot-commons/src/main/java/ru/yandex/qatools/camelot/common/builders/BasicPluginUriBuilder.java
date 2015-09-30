package ru.yandex.qatools.camelot.common.builders;

import ru.yandex.qatools.camelot.common.PluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class BasicPluginUriBuilder implements PluginUriBuilder {

    @Override
    public String broadcastUri(String pluginId, String suffix) {
        return localUri(pluginId, suffix);
    }

    @Override
    public String localUri(String pluginId, String suffix) {
        return "seda:plugin." + pluginId + ((isEmpty(suffix)) ? "" : "." + suffix);
    }

    @Override
    public String pluginInputUri(Plugin plugin, String suffix, String brokerConfig) {
        return plugin.getBaseInputUri() +
                "." + plugin.getId() +
                ((isEmpty(suffix)) ? "" : "." + suffix) +
                ((isEmpty(brokerConfig)) ? "" : suffix);
    }

    @Override
    public String tmpInputBufferUri() {
        return localUri("tmp", "input");
    }

    @Override
    public String frontendBroadcastUri() {
        return localUri("frontend", "input");
    }

    @Override
    public String basePluginInputUri() {
        return "seda:plugin";
    }
}
