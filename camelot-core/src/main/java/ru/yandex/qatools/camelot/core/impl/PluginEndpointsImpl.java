package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.PluginUriBuilder;

import static ru.yandex.qatools.camelot.Constants.*;
import static ru.yandex.qatools.camelot.util.NameUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginEndpointsImpl implements PluginEndpoints {
    protected final Plugin plugin;
    final String inputUri;
    final String delayedInputUri;
    final String outputUri;
    final String splitUri;
    final String filteredUri;
    final String producerUri;
    final String consumerUri;
    final String clientSendUri;
    final String broadcastFrontendUri;
    final String mainInputUri;
    final String endpointListenerUri;
    final String inputRouteId;
    final String delayedInputRouteId;
    final String outputRouteId;
    final String splitRouteId;
    final String filteredRouteId;
    final String producerRouteId;
    final String consumerRouteId;
    final String clientSendRouteId;
    final String mainInputRouteId;
    final String endpointListenerRouteId;
    final String engineName;

    public PluginEndpointsImpl(String mainInputUri, Plugin plugin, String engineName, PluginUriBuilder uriBuilder) {
        final String brokerConfig = pluginBrokerConfig(plugin);
        this.engineName = engineName;
        this.plugin = plugin;
        this.mainInputUri = mainInputUri;
        this.inputUri = uriBuilder.pluginInputUri(plugin, "", brokerConfig);
        this.delayedInputUri = uriBuilder.pluginInputUri(plugin, DELAYED_SUFFIX, "");
        this.outputUri = uriBuilder.localUri(plugin.getId(), OUTPUT_SUFFIX);
        this.splitUri = uriBuilder.localUri(plugin.getId(), SPLIT_SUFFIX);
        this.filteredUri = uriBuilder.localUri(plugin.getId(), FILTERED_SUFFIX);
        this.consumerUri = uriBuilder.localUri(plugin.getId(), CONSUMER_SUFFIX);
        this.producerUri = uriBuilder.localUri(plugin.getId(), PRODUCER_SUFFIX);
        this.clientSendUri = uriBuilder.localUri(plugin.getId(), CLIENT_SEND_SUFFIX);
        this.endpointListenerUri = uriBuilder.broadcastUri(plugin.getId(), RES_LISTENER_SUFFIX);
        this.broadcastFrontendUri = uriBuilder.frontendBroadcastUri();

        this.mainInputRouteId = mainInputUri + engineName;
        this.inputRouteId = pluginInputRouteId(inputUri, engineName);
        this.delayedInputRouteId = pluginInputRouteId(delayedInputUri, engineName);
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
    public String getDelayedInputUri() {
        return delayedInputUri;
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
    public String getFrontendSendUri() {
        return clientSendUri;
    }

    @Override
    public String getBroadcastFrontendUri() {
        return broadcastFrontendUri;
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
    public String getDelayedInputRouteId() {
        return delayedInputRouteId;
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
    public String getFrontendSendRouteId() {
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
