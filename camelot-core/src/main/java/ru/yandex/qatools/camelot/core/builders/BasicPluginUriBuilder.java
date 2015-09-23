package ru.yandex.qatools.camelot.core.builders;

import ru.yandex.qatools.camelot.core.PluginUriBuilder;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public abstract class BasicPluginUriBuilder implements PluginUriBuilder {

    @Override
    public String localUri(String pluginId, String suffix) {
        return "direct:plugin." + pluginId + ((isEmpty(suffix)) ? "" : "." + suffix);
    }
}
