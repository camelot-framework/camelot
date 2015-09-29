package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;
import ru.yandex.qatools.camelot.beans.AggregatorConfig;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.builder.ExpressionBuilder.beanExpression;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AggregatorPluginRouteBuilder extends GenericPluginRouteBuilder implements AggregatorRoutesBuilder {
    private AggregationStrategyBuilder strategyBuilder;

    public AggregatorPluginRouteBuilder(CamelContext camelContext, Plugin aggregatorPlugin) throws Exception { //NOSONAR
        super(aggregatorPlugin, camelContext);
        this.strategyBuilder = newAggregationStrategyBuilder(plugin.getContext());
    }

    @Override
    public void configure() throws Exception { //NOSONAR
        super.configure();

        // Initialize the aggregation method builder
        Processor strategy = getStrategyBuilder().build();
        AggregatorConfig aggregatorConfig = getStrategyBuilder().getConfig();

        addInterimProc(from(endpoints.getDelayedInputUri())
                .delay(plugin.getContext().getAppConfig().getLong("camelot.delayedRoute.delay.ms"))
                .log(DEBUG, pluginId + " delayed ${exchangeId} ${in.header.bodyClass}, correlationKey: ${in.header.correlationKey}"))
                .to(endpoints.getConsumerUri());

        // init default first inputRoute endpoint
        final RouteDefinition inputRoute = appendSplitterRoutes(
                appendFilterRoutes(
                        from(endpoints.getInputUri())
                )
        );

        final Expression aggKey = aggKeyExpression(aggregatorConfig);
        // main aggregation inputRoute
        final RouteDefinition definition = addInterimProc(inputRoute
                .setHeader(PLUGIN_ID, constant(pluginId))
                .setHeader(CORRELATION_KEY, aggKey)
                .log(DEBUG, format("===> INPUT FOR %s ${exchangeId} ${in.header.bodyClass}, correlationKey: ${in.header.correlationKey}", pluginId))
                .process(strategy));


        definition
                .choice()
                .when(body().isNull())
                    .log(DEBUG, pluginId + " output is NULL, skipping next routes")
                    .stop()
                .otherwise()
                    .log(DEBUG, format("===> ROUTE %s ===> %s", endpoints.getInputUri(), endpoints.getProducerUri()))
                    .to(endpoints.getProducerUri())
                .endChoice()
                .routeId(endpoints.getInputRouteId());

    }

    @Override
    public AggregationStrategyBuilder getStrategyBuilder() {
        return strategyBuilder;
    }

    @Override
    public void setStrategyBuilder(AggregationStrategyBuilder strategyBuilder) {
        this.strategyBuilder = strategyBuilder;
    }

    private CamelotAggregationStrategyBuilder newAggregationStrategyBuilder(PluginContext context) throws Exception { //NOSONAR
        return new CamelotAggregationStrategyBuilder(camelContext, context.getPluginClass(), context);
    }

    private Expression aggKeyExpression(AggregatorConfig aggregatorConfig) throws Exception { //NOSONAR
        return beanExpression(aggregatorConfig.getStrategyInstance(), "aggregationKey");
    }
}
