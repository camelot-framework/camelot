package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;
import static org.apache.camel.util.ExchangeHelper.createCorrelatedCopy;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class CamelotAggregationStrategy extends FSMAggregationStrategy implements Processor {

    private final PluginContext context;

    public CamelotAggregationStrategy(
            ClassLoader classLoader, Object fsmEngineBuilder, PluginContext context)
            throws NoSuchMethodException, ClassNotFoundException {
        super(classLoader, classLoader.loadClass(context.getPluginClass()), fsmEngineBuilder);
        this.context = context;
        this.setSerializeMessages(true);
    }

    @Override
    public void process(Exchange message) {
        if (context.isShuttingDown()) {
            logger.warn("Message is not going to be aggregated due to context shutting down: "
                    + message.getIn().getBody());
            return;
        }
        final String key = (String) message.getIn().getHeader(CORRELATION_KEY);
        if (isEmpty(key)) {
            logger.warn(format("Empty keys are not allowed! " +
                    "For plugin %s : skipping aggregation!", key));
            return;
        }
        final AggregationRepository repo = context.getAggregationRepo();
        final Exchange state = repo.get(camelContext, key);
        try {
            final Exchange result = createCorrelatedCopy(super.aggregate(state, message), false);
            if (isCompleted(result)) {
                message.setIn(result.getIn());
                repo.remove(camelContext, key, result);
            } else {
                message.getIn().setBody(null);
                repo.add(camelContext, key, result);
            }
        } catch (InvocationTargetException e) {
            repo.confirm(camelContext, key);
            logger.trace("Sonar trick", e);
            logger.error(format("Failed to aggregate for plugin %s with key '%s': %s! \n %s",
                    context.getId(), key, e.getMessage(), formatStackTrace(e.getTargetException())),
                    e.getTargetException());
        } catch (Exception e) {
            repo.confirm(camelContext, key);
            logger.error(format("Failed to aggregate for plugin %s with key '%s': %s",
                    context.getId(), key, e.getMessage()), e);
        }
    }

    @Override
    protected void injectFields(Object procInstance, Exchange exchange) {
        context.getInjector().inject(procInstance, context, exchange);
    }
}
