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
        final String key = (String) message.getIn().getHeader(CORRELATION_KEY);
        if (isEmpty(key)) {
            logger.error("Empty keys are not allowed! SKIPPING MESSAGE: {}", message);
            return;
        }

        if (context.isShuttingDown()) {
            logger.warn("Context is shutting down, resending message: {}",
                    message.getIn().getBody());
            resendWithDelay(message);
        }

        final AggregationRepository repo = context.getAggregationRepo();
        try {
            processOrDie(message, key, repo);
        } catch (RepositoryUnreachableException e) {
            // resend with delay
            logger.warn("Repository is unreachable for plugin '{}', resending message: {}",
                    context.getId(), message);
            resendWithDelay(message);
        } catch (RepositoryFailureException e) {
            // skip message
            logger.error("Repository failure occurred for plugin '{}', SKIPPING MESSAGE: {}",
                    context.getId(), message, e);
        } catch (InvocationTargetException e) {
            // sonar trick
            repo.confirm(camelContext, key);
            logger.trace("Sonar trick", e);
            logger.error("Failed to aggregate for plugin '{}', SKIPPING MESSAGE: {} \n {}",
                    context.getId(), message, formatStackTrace(e.getTargetException()),
                    e.getTargetException());
        } catch (Exception e) {
            repo.confirm(camelContext, key);
            logger.error("Failed to aggregate for plugin '{}', SKIPPING MESSAGE: {}",
                    context.getId(), message, e);
        }
    }

    private void resendWithDelay(Exchange message) {
        if (retryProducer == null) {
            retryProducer = camelContext.createProducerTemplate();
            retryProducer.setDefaultEndpointUri(context.getEndpoints().getDelayedInputUri());
        }
        retryProducer.send(message);
    }

    private void processOrDie(Exchange message, String key, AggregationRepository repo)
            throws RepositoryFailureException, RepositoryUnreachableException,
                   NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Exchange state = repo.get(camelContext, key);
        final Exchange result = createCorrelatedCopy(super.aggregate(state, message), false);
        if (isCompleted(result)) {
            message.setIn(result.getIn());
            repo.remove(camelContext, key, result);
        } else {
            message.getIn().setBody(null);
            repo.add(camelContext, key, result);
        }
    }

    @Override
    protected void injectFields(Object procInstance, Exchange exchange) {
        context.getInjector().inject(procInstance, context, exchange);
    }
}
