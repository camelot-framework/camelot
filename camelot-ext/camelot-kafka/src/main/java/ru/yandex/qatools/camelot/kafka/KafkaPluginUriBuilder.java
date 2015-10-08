package ru.yandex.qatools.camelot.kafka;

import ru.yandex.qatools.camelot.common.builders.BasicPluginUriBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class KafkaPluginUriBuilder extends BasicPluginUriBuilder {

    private final String kafkaHosts;
    private final String zkHosts;
    private final String defaultConfig;

    public KafkaPluginUriBuilder(String kafkaHosts, String zkHosts, String defaultConfig) {
        this.kafkaHosts = kafkaHosts;
        this.zkHosts = zkHosts;
        this.defaultConfig = isEmpty(defaultConfig) ? "groupId=" + randomUUID().toString() : defaultConfig;
    }

    @Override
    public String pluginUri(Plugin plugin, String suffix, String brokerConfig) {
        return kafkaTopicUri(plugin.getBaseInputUri(), plugin.getId()
                + (isEmpty(suffix) ? "" : "." + suffix), brokerConfig);
    }

    @Override
    public String broadcastUri(String pluginId, String suffix) {
        return kafkaTopicUri(kafkaBaseUri(), pluginId + (isEmpty(suffix) ? "" : "." + suffix), "");
    }

    @Override
    public String tmpInputBufferUri() {
        return kafkaTopicUri(kafkaBaseUri(), "all.tmp.input.buffer", "");
    }

    @Override
    public String frontendBroadcastUri() {
        return kafkaTopicUri(kafkaBaseUri(), "all.frontend.notify", "");
    }

    public String kafkaBaseUri() {
        return format("kafka:%s?zookeeperConnect=%s" +
                "&serializerClass=kafka.serializer.DefaultEncoder" +
                "&requestRequiredAcks=1" +
                "&autoOffsetReset=smallest", kafkaHosts, zkHosts);
    }

    public String kafkaTopicUri(String kafkaBaseUri, String topic, String brokerConfig) {
        String config = isEmpty(brokerConfig) ? "" : "&" + brokerConfig.substring(1);
        if (config.endsWith("&")) {
            config = config.substring(0, config.lastIndexOf("&"));
        }
        return format("%s&topic=%s&%s%s", kafkaBaseUri, topic, defaultConfig, config);
    }

    @Override
    public String basePluginUri() {
        return kafkaBaseUri();
    }
}
