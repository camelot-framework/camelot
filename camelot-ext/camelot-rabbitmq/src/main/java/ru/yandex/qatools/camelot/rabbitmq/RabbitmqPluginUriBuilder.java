package ru.yandex.qatools.camelot.rabbitmq;

import ru.yandex.qatools.camelot.common.builders.BasicPluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author Ilya Sadykov
 */
public class RabbitmqPluginUriBuilder extends BasicPluginUriBuilder {

    private final String username;
    private final String password;
    private final String firstHost;
    private final String rabbitmqHosts;
    private final String defaultConfig;

    public RabbitmqPluginUriBuilder(String rabbitmqHosts, String defaultConfig) {
        this(rabbitmqHosts, null, null, defaultConfig);
    }

    public RabbitmqPluginUriBuilder(String rabbitmqHosts, String username, String password, String defaultConfig) {
        this.username = username;
        this.password = password;
        this.rabbitmqHosts = rabbitmqHosts;
        this.firstHost = rabbitmqHosts.split(",")[0];
        this.defaultConfig = defaultConfig;
    }

    @Override
    public String pluginUri(Plugin plugin, String suffix, String brokerConfig) {
        return rabbitmqUri(plugin.getBaseInputUri(), plugin.getId()
                + (isEmpty(suffix) ? "" : "." + suffix), brokerConfig);
    }

    @Override
    public String frontendBroadcastUri() {
        return rabbitmqUri(rabbitmqBaseUri(), "frontend.notify", "?exchangeType=topic");
    }

    public String rabbitmqBaseUri() {
        return format("rabbitmq://%s/", firstHost);
    }

    public String rabbitmqUri(String baseUri, String name, String brokerConfig) {
        String config = isEmpty(brokerConfig) ? "" : "&" + brokerConfig.substring(1);
        if (config.endsWith("&")) {
            config = config.substring(0, config.lastIndexOf("&"));
        }
        final String fmtConfig = !isEmpty(this.defaultConfig) ? "&" + this.defaultConfig : "";
        return format("%s%s?queue=%s%s%s%s", baseUri, name, name, connectionOpts(), fmtConfig, config);
    }

    private String connectionOpts() {
        return join(
                "&addresses=" + rabbitmqHosts,
                isEmpty(username) ? "" : "&username=" + username,
                isEmpty(password) ? "" : "&password=" + password
        );
    }

    @Override
    public String basePluginUri() {
        return rabbitmqBaseUri();
    }
}
