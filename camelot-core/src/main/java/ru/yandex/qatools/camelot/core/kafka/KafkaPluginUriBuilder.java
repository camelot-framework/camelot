package ru.yandex.qatools.camelot.core.kafka;

import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.builders.BasicPluginUriBuilder;

import static java.lang.String.format;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * @author Ilya Sadykov
 */
public class KafkaPluginUriBuilder extends BasicPluginUriBuilder {

    private final String kafkaHosts;
    private final String zkHost;
    private final int zkPort;

    public KafkaPluginUriBuilder(String kafkaHosts, String zkHost, int zkPort) {
        this.kafkaHosts = kafkaHosts;
        this.zkHost = zkHost;
        this.zkPort = zkPort;
    }

    @Override
    public String pluginInputUri(Plugin plugin, String suffix, String brokerConfig) {
        return kafkaTopicUri(plugin.getBaseInputUri(), plugin.getId() + ".input"
                + (isEmpty(suffix) ? "" : "." + suffix), plugin.getId(), brokerConfig);
    }

    @Override
    public String broadcastUri(String pluginId, String suffix) {
        return kafkaTopicUri(kafkaBaseUri(), pluginId + (isEmpty(suffix) ? "" : "." + suffix), "all", "");
    }

    @Override
    public String tmpInputBufferUri() {
        return kafkaTopicUri(kafkaBaseUri(), "all.tmp.input.buffer", "all", "");
    }

    @Override
    public String frontendBroadcastUri() {
        return kafkaTopicUri(kafkaBaseUri(), "all.frontend.notify", "all", "");
    }

    public String kafkaBaseUri() {
        return format("kafka:%s?zookeeperHost=%s&zookeeperPort=%s" +
                "&serializerClass=kafka.serializer.DefaultEncoder" +
                "&partitioner=kafka.producer.DefaultPartitioner" +
                "&requestRequiredAcks=1" +
                "&autoOffsetReset=smallest", kafkaHosts, zkHost, zkPort);
    }

    public String kafkaTopicUri(String kafkaBaseUri, String topic, String groupId, String brokerConfig) {
        String config = isEmpty(brokerConfig) ? "" : "&" + brokerConfig.substring(1);
        if (config.endsWith("&")) {
            config = config.substring(0, config.lastIndexOf("&"));
        }
        return format("%s&topic=%s&groupId=%s%s", kafkaBaseUri, topic, groupId, config);
    }

    @Override
    public String basePluginInputUri() {
        return kafkaBaseUri();
    }
}
