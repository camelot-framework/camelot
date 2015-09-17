package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.beans.RouteConfig;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.InterimProcessor;
import ru.yandex.qatools.camelot.core.SplitStrategy;
import ru.yandex.qatools.camelot.core.impl.InstanceOfFilter;
import ru.yandex.qatools.camelot.core.impl.PluginMessageFilter;
import ru.yandex.qatools.camelot.core.impl.RouteConfigReader;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.builder.ExpressionBuilder.beanExpression;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;
import static ru.yandex.qatools.camelot.util.ServiceUtil.*;

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
    protected final InterimProcessor interimProc;

    final PluginEndpoints endpoints;

    public GenericPluginRouteBuilder(Plugin plugin, CamelContext camelContext) throws Exception {
        this.camelContext = camelContext;
        this.classLoader = plugin.getContext().getClassLoader();
        this.pluginId = plugin.getId();
        this.plugin = plugin;
        this.endpoints = plugin.getContext().getEndpoints();
        this.routeConfig = new RouteConfigReader(plugin.getContext()).read();
        this.interimProc = plugin.getContext().getInterimProcessor();
    }

    @Override
    public void configure() throws Exception {
        // Initialize the global output producers
        addInterimProc(from(endpoints.getProducerUri()).setHeader(PLUGIN_ID, constant(pluginId)).
                log(DEBUG, format("===> ROUTE %s ===> %s", endpoints.getProducerUri(), endpoints.getOutputUri()))).
                to(endpoints.getOutputUri()).
                routeId(endpoints.getProducerRouteId());

        // Initialize the global input producers
        addInterimProc(from(endpoints.getConsumerUri()).
                log(DEBUG, format("===> ROUTE %s ===> %s", endpoints.getConsumerUri(), endpoints.getInputUri())).
                setHeader(PLUGIN_ID, constant(pluginId))).
                to(endpoints.getInputUri()).
                routeId(endpoints.getConsumerRouteId());

        // Initialize client notify topic
        addInterimProc(from(endpoints.getFrontendSendUri()).setHeader(PLUGIN_ID, constant(pluginId)).
                log(DEBUG, format("CLIENT NOTIFY FOR %s TO %s", pluginId + HEADER_BODY_CLASS, endpoints.getBroadcastFrontendUri()))).
                to(endpoints.getBroadcastFrontendUri()).
                routeId(endpoints.getFrontendSendRouteId());
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
            addInterimProc(route.split(splitExpression())).
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
            addInterimProc(route.filter()
                    .method(new InstanceOfFilter(classLoader, plugin.getContext().getMessagesSerializer(), routeConfig.getFilterInstanceOf()),
                            InstanceOfFilter.FILTER_METHOD_NAME))
                    .to(endpoints.getFilteredUri())
                    .routeId(endpoints.getFilteredRouteId());
            return from(endpoints.getFilteredUri());
        } else if (routeConfig.getCustomFilter() != null) {
            addInterimProc(route.filter()
                    .method(
                            new PluginMessageFilter(plugin, routeConfig.getCustomFilter()),
                            PluginMessageFilter.FILTER_METHOD_NAME))
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
                endpoints.getFrontendSendRouteId(),
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

    protected <T extends ProcessorDefinition> T addInterimProc(T route) {
        if (interimProc != null) {
            route.process(interimProc);
        }
        return route;
    }
}
