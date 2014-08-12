package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.beans.AggregationOptions;
import ru.yandex.qatools.camelot.beans.AggregatorConfig;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.impl.CamelotAggregationStrategy;

import static org.apache.camel.LoggingLevel.DEBUG;
import static org.apache.camel.LoggingLevel.TRACE;
import static org.apache.camel.builder.ExpressionBuilder.beanExpression;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AggregatorPluginRouteBuilder extends GenericPluginRouteBuilder implements AggregatorRoutesBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorPluginRouteBuilder.class);
    private AggregationStrategyBuilder strategyBuilder;
    private final AggregationOptions opts;

    public AggregatorPluginRouteBuilder(CamelContext camelContext, Plugin aggregatorPlugin, AggregationOptions options) throws Exception {
        super(aggregatorPlugin, camelContext);
        this.strategyBuilder = newAggregationStrategyBuilder(plugin.getContext());
        this.opts = options;
    }

    @Override
    public void configure() throws Exception {
        super.configure();

        // Initialize the aggregation method builder
        CamelotAggregationStrategy strategy = getStrategyBuilder().build();
        AggregatorConfig aggregatorConfig = getStrategyBuilder().getConfig();

        // init default first route endpoint
        RouteDefinition route = appendSplitterRoutes(
                appendFilterRoutes(
                        from(endpoints.getInputUri())
                )
        );

        final Expression aggKey = aggKeyExpression(aggregatorConfig);
        // main aggregation route
        final RouteDefinition definition = route.
                setHeader(PLUGIN_ID, constant(pluginId))
                .setHeader(CORRELATION_KEY, aggKey)
                .log(DEBUG, pluginId + " input ${in.header.bodyClass}, correlationKey: ${in.header.correlationKey}")
                .process(strategy);

        definition
                .choice()
                .when(body().isNull()).log(TRACE, pluginId + " output is NULL, skipping next routes").stop()
                .otherwise().to(endpoints.getOutputUri())
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

    private CamelotAggregationStrategyBuilder newAggregationStrategyBuilder(PluginContext context) throws Exception {
        return new CamelotAggregationStrategyBuilder(context.getPluginClass(), context);
    }

    private Expression aggKeyExpression(AggregatorConfig aggregatorConfig) throws Exception {
        return beanExpression(aggregatorConfig.getStrategyInstance(), "aggregationKey");
    }

    /**
     * Initializes the completion predicate
     */
    private Predicate completionPredicate(final CamelotAggregationStrategy strategy) {
        return new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                try {
                    return strategy.isCompleted(exchange);
                } catch (Exception e) {
                    LOGGER.warn("Completion predicate calculation failed", e);
                    return false;
                }
            }
        };
    }
}
