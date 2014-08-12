package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.beans.RouteConfig;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.SplitStrategy;
import ru.yandex.qatools.camelot.core.impl.InstanceOfFilter;
import ru.yandex.qatools.camelot.core.impl.PluginMessageFilter;
import ru.yandex.qatools.camelot.core.impl.RouteConfigReader;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.builder.ExpressionBuilder.beanExpression;
import static ru.yandex.qatools.camelot.Constants.BROADCAST_CONFIG;
import static ru.yandex.qatools.camelot.Constants.CLIENT_NOTIFY_URI;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveEndpoints;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveRoute;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyStartRoute;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class GenericPluginRouteBuilder extends RouteBuilder implements BasicRoutesBuilder {
    public static final String HEADER_BODY_CLASS = ": ${in.header.bodyClass}";

    protected final String pluginId;
    protected final CamelContext camelContext;
    protected final ClassLoader classLoader;
    protected final Plugin plugin;
    protected final RouteConfig routeConfig;

    final PluginEndpoints endpoints;

    public GenericPluginRouteBuilder(Plugin plugin, CamelContext camelContext) throws Exception {
        this.camelContext = camelContext;
        this.classLoader = plugin.getContext().getClassLoader();
        this.pluginId = plugin.getId();
        this.plugin = plugin;
        this.endpoints = plugin.getContext().getEndpoints();
        this.routeConfig = new RouteConfigReader(plugin.getContext()).read();
    }

    @Override
    public void configure() throws Exception {
        // Initialize the global output producers
        from(endpoints.getProducerUri()).setHeader(PLUGIN_ID, constant(pluginId)).
                log(DEBUG, "OUTPUT FROM " + pluginId + HEADER_BODY_CLASS).
                to(endpoints.getOutputUri()).
                routeId(endpoints.getProducerRouteId());

        // Initialize the global input producers
        from(endpoints.getConsumerUri()).
                log(DEBUG, "INPUT TO " + pluginId + HEADER_BODY_CLASS).
                setHeader(PLUGIN_ID, constant(pluginId)).
                to(endpoints.getInputUri()).
                routeId(endpoints.getConsumerRouteId());

        // Initialize client notify topic
        from(endpoints.getClientSendUri()).setHeader(PLUGIN_ID, constant(pluginId)).
                to(CLIENT_NOTIFY_URI + BROADCAST_CONFIG).
                routeId(endpoints.getClientSendRouteId());
    }


    /**
     * Startup routes associated with the plugin from Context
     */
    @Override
    public void startRoutes() throws Exception {
        for (String routeId : getRoutesList()) {
            startRoute(routeId);
        }
    }

    /**
     * Remove routes associated with the plugin from Context
     * Useful while plugins reloading
     */
    @Override
    public void removeRoutes() throws Exception {
        for (String routeId : getRoutesList()) {
            stopRoute(routeId);
        }
    }

    protected RouteDefinition appendSplitterRoutes(RouteDefinition route) throws Exception {
        // Splitter (optional)
        if (routeConfig.getSplitStrategy() != null) {
            route.split(splitExpression()).
                    parallelProcessing().
                    to(endpoints.getSplitUri()).
                    routeId(endpoints.getSplitRouteId());
            return from(endpoints.getSplitUri());
        }
        return route;
    }

    protected RouteDefinition appendFilterRoutes(RouteDefinition route) {
        // Filtered (optional)
        if (routeConfig.getFilterInstanceOf() != null) {
            route.filter()
                    .method(new InstanceOfFilter(classLoader, routeConfig.getFilterInstanceOf()),
                            InstanceOfFilter.FILTER_METHOD_NAME)
                    .to(endpoints.getFilteredUri())
                    .routeId(endpoints.getFilteredRouteId());
            return from(endpoints.getFilteredUri());
        } else if (routeConfig.getCustomFilter() != null) {
            route.filter()
                    .method(
                            new PluginMessageFilter(plugin, routeConfig.getCustomFilter()),
                            PluginMessageFilter.FILTER_METHOD_NAME)
                    .to(endpoints.getFilteredUri())
                    .routeId(endpoints.getFilteredRouteId());
            return from(endpoints.getFilteredUri());
        }
        return route;
    }

    protected Expression splitExpression() throws Exception {
        return beanExpression(routeConfig.getSplitStrategy(), SplitStrategy.SPLIT_METHOD_NAME);
    }

    protected String[] getRoutesList() {
        return new String[]{
                endpoints.getInputRouteId(),
                endpoints.getOutputRouteId(),
                endpoints.getConsumerRouteId(),
                endpoints.getClientSendRouteId(),
                endpoints.getSplitRouteId(),
                endpoints.getFilteredRouteId()
        };
    }

    protected void startRoute(String id) throws Exception {
        gracefullyStartRoute(camelContext, id);
    }

    protected void stopRoute(String id) throws Exception {
        gracefullyRemoveRoute(camelContext, id);
        gracefullyRemoveEndpoints(camelContext, id);
    }

}
