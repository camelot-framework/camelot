package ru.yandex.qatools.camelot.api;

/**
 * Describes the single plugin endpoints
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginEndpoints {
    /**
     * The main input uri (equals to the Engine's input uri)
     */
    String getMainInputUri();

    /**
     * The main input route id (equals to the Engine's input uri)
     */
    String getMainInputRouteId();

    /**
     * Input uri for the plugin
     */
    String getInputUri();

    /**
     * Input uri for the plugin with delay
     */
    String getDelayedInputUri();

    /**
     * Input route id for the plugin
     */
    String getInputRouteId();

    /**
     * Input route id for the plugin
     */
    String getDelayedInputRouteId();

    /**
     * Output uri for the plugin
     */
    String getOutputUri();

    /**
     * Output plugin route id for the plugin
     */
    String getOutputRouteId();

    /**
     * Temporary intermediate uri to build the route which uses the splitting
     */
    String getSplitUri();

    /**
     * Temporary intermediate route id to build the route which uses the splitting
     */
    String getSplitRouteId();

    /**
     * Temporary intermediate uri to build the route which uses the filtering
     */
    String getFilteredUri();

    /**
     * Temporary intermediate route id to build the route which uses the filtering
     */
    String getFilteredRouteId();

    /**
     * Temporary intermediate uri to build the route's output
     */
    String getProducerUri();

    /**
     * Temporary intermediate route id to build the route's output
     */
    String getProducerRouteId();

    /**
     * Temporary intermediate uri to build the route's input
     */
    String getConsumerUri();

    /**
     * Temporary intermediate route id to build the route's input
     */
    String getConsumerRouteId();

    /**
     * Plugin's uri to produce the frontend messages (to websockets)
     */
    String getFrontendSendUri();

    /**
     * Global uri to broadcast message to all frontend clients
     */
    String getBroadcastFrontendUri();

    /**
     * Plugin's route id to produce the frontend messages (to websockets)
     */
    String getFrontendSendRouteId();

    /**
     * Get plugins system engine name
     */
    String getEngineName();
}
