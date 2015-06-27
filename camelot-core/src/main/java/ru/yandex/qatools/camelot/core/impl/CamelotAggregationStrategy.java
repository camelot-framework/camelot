package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.error.RepositoryFailureException;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;
import static org.apache.camel.util.ExchangeHelper.createCorrelatedCopy;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class CamelotAggregationStrategy extends FSMAggregationStrategy implements Processor {

    private final PluginContext context;
    private volatile ProducerTemplate retryProducer;

    public CamelotAggregationStrategy(
            CamelContext camelContext,
            ClassLoader classLoader, Object fsmEngineBuilder, PluginContext context)
            throws NoSuchMethodException, ClassNotFoundException {
        super(classLoader, classLoader.loadClass(context.getPluginClass()), fsmEngineBuilder);
        this.setCamelContext(camelContext);
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
        try {
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
        } catch (RepositoryFailureException e) {
            // skip message
            logger.warn("Repository failure is occurred for plugin '{}' and key '{}', SKIPPING EXCHANGE!...",
                    context.getId(), key, e);
        } catch (RepositoryUnreachableException e) {
            // resend with delay
            if (retryProducer == null) {
                retryProducer = camelContext.createProducerTemplate();
                retryProducer.setDefaultEndpointUri(context.getEndpoints().getDelayedInputUri());
            }
            logger.warn("Repository is unreachable for plugin '{}' and key '{}', " +
                            "retrying with delay for body of {}...",
                    context.getId(), key, message.getIn().getHeader(BODY_CLASS));
            retryProducer.send(message);
        }
    }

    @Override
    protected void injectFields(Object procInstance, Exchange exchange) {
        context.getInjector().inject(procInstance, context, exchange);
    }
}
