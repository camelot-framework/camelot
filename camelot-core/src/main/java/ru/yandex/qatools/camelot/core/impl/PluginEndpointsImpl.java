package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.config.Plugin;

import static ru.yandex.qatools.camelot.Constants.BROADCAST_CONFIG;
import static ru.yandex.qatools.camelot.Constants.CLIENT_SEND_POSTFIX;
import static ru.yandex.qatools.camelot.Constants.CONSUMER_POSTFIX;
import static ru.yandex.qatools.camelot.Constants.FILTERED_POSTFIX;
import static ru.yandex.qatools.camelot.Constants.PRODUCER_POSTFIX;
import static ru.yandex.qatools.camelot.Constants.RES_LISTENER_POSTFIX;
import static ru.yandex.qatools.camelot.Constants.SPLIT_POSTFIX;
import static ru.yandex.qatools.camelot.util.NameUtil.broadcastRouteId;
import static ru.yandex.qatools.camelot.util.NameUtil.broadcastUri;
import static ru.yandex.qatools.camelot.util.NameUtil.localRouteId;
import static ru.yandex.qatools.camelot.util.NameUtil.localUri;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginBrokerConfig;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginInputRouteId;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginInputUri;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginOutputRouteId;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginOutputUri;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginEndpointsImpl implements PluginEndpoints {
    final String inputUri;
    final String outputUri;
    final String splitUri;
    final String filteredUri;
    final String producerUri;
    final String consumerUri;
    final String clientSendUri;
    final String mainInputUri;
    final String endpointListenerUri;
    final String inputRouteId;
    final String outputRouteId;
    final String splitRouteId;
    final String filteredRouteId;
    final String producerRouteId;
    final String consumerRouteId;
    final String clientSendRouteId;
    final String mainInputRouteId;
    final String endpointListenerRouteId;
    final String engineName;
    protected final Plugin plugin;

    public PluginEndpointsImpl(String mainInputUri, Plugin plugin, String engineName) {
        final String brokerConfig = pluginBrokerConfig(plugin);
        this.engineName = engineName;
        this.plugin = plugin;
        this.mainInputUri = mainInputUri;
        this.inputUri = pluginInputUri(plugin) + brokerConfig;
        this.outputUri = pluginOutputUri(plugin) + brokerConfig;
        this.splitUri = localUri(plugin.getId(), SPLIT_POSTFIX);
        this.filteredUri = localUri(plugin.getId(), FILTERED_POSTFIX);
        this.consumerUri = localUri(plugin.getId(), CONSUMER_POSTFIX);
        this.producerUri = localUri(plugin.getId(), PRODUCER_POSTFIX);
        this.clientSendUri = localUri(plugin.getId(), CLIENT_SEND_POSTFIX);
        this.endpointListenerUri = broadcastUri(plugin.getId(), RES_LISTENER_POSTFIX) + BROADCAST_CONFIG;

        this.mainInputRouteId = mainInputUri + engineName;
        this.inputRouteId = pluginInputRouteId(inputUri, engineName);
        this.outputRouteId = pluginOutputRouteId(outputUri, engineName);
        this.splitRouteId = localRouteId(splitUri, engineName);
        this.filteredRouteId = localRouteId(filteredUri, engineName);
        this.consumerRouteId = localRouteId(consumerUri, engineName);
        this.producerRouteId = localRouteId(producerUri, engineName);
        this.clientSendRouteId = localRouteId(clientSendUri, engineName);
        this.endpointListenerRouteId = broadcastRouteId(endpointListenerUri, engineName);
    }

    @Override
    public String getInputUri() {
        return inputUri;
    }

    @Override
    public String getOutputUri() {
        return outputUri;
    }

    @Override
    public String getSplitUri() {
        return splitUri;
    }

    @Override
    public String getFilteredUri() {
        return filteredUri;
    }

    @Override
    public String getProducerUri() {
        return producerUri;
    }

    @Override
    public String getConsumerUri() {
        return consumerUri;
    }

    @Override
    public String getClientSendUri() {
        return clientSendUri;
    }

    @Override
    public String getMainInputUri() {
        return mainInputUri;
    }

    @Override
    public String getEndpointListenerUri() {
        return endpointListenerUri;
    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public String getInputRouteId() {
        return inputRouteId;
    }

    @Override
    public String getOutputRouteId() {
        return outputRouteId;
    }

    @Override
    public String getSplitRouteId() {
        return splitRouteId;
    }

    @Override
    public String getFilteredRouteId() {
        return filteredRouteId;
    }

    @Override
    public String getProducerRouteId() {
        return producerRouteId;
    }

    @Override
    public String getConsumerRouteId() {
        return consumerRouteId;
    }

    @Override
    public String getClientSendRouteId() {
        return clientSendRouteId;
    }

    @Override
    public String getMainInputRouteId() {
        return mainInputRouteId;
    }

    @Override
    public String getEndpointListenerRouteId() {
        return endpointListenerRouteId;
    }
}
