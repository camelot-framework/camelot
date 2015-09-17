package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.impl.CamelotProcessor;

import static java.lang.String.format;
import static org.apache.camel.LoggingLevel.DEBUG;
import static ru.yandex.qatools.camelot.api.Constants.Headers.PLUGIN_ID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ProcessorPluginRouteBuilder extends GenericPluginRouteBuilder implements ProcessorRoutesBuilder {
    protected Processor processor;

    public ProcessorPluginRouteBuilder(CamelContext camelContext, Plugin processorPlugin) throws Exception {
        super(processorPlugin, camelContext);
        this.processor = initProcessor(classLoader.loadClass(plugin.getContext().getPluginClass()));
    }

    @Override
    public void configure() throws Exception {
        super.configure();

        // initial route
        RouteDefinition route = appendSplitterRoutes(
                appendFilterRoutes(
                        from(endpoints.getInputUri())
                )
        );


        // main processing route
        addInterimProc(route
                .setHeader(PLUGIN_ID, constant(pluginId))
                .log(DEBUG,  format("===> INPUT FOR %s ${exchangeId} ${in.header.bodyClass}, correlationKey: ${in.header.correlationKey}", pluginId))
                .process(getProcessor()))
                .choice()
                .when(body().isNull())
                    .log(DEBUG, pluginId + " output is NULL, skipping next routes")
                    .stop()
                .otherwise()
                    .log(DEBUG, format("===> ROUTE %s ===> %s", endpoints.getInputUri(), endpoints.getProducerUri()))
                    .to(endpoints.getProducerUri())
                .routeId(endpoints.getInputRouteId());

    }

    @Override
    public Processor getProcessor() {
        return processor;
    }

    @Override
    public void setProcessor(Processor processor) {
        this.processor = processor;
    }

    private CamelotProcessor initProcessor(Class<?> procClass) throws InstantiationException, IllegalAccessException {
        return new CamelotProcessor(classLoader, procClass, plugin.getContext());
    }
}
