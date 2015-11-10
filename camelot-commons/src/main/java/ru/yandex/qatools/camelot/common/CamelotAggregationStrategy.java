package ru.yandex.qatools.camelot.common;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.error.*;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.lang.reflect.InvocationTargetException;

import static org.apache.camel.util.ExchangeHelper.createCorrelatedCopy;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class CamelotAggregationStrategy extends FSMAggregationStrategy implements Processor {

    private static final String MESSAGE_RESENT_ID_HEADER = "MESSAGE_RESENT_ID";

    private final PluginContext context;
    private volatile ProducerTemplate retryProducer;

    public CamelotAggregationStrategy(
            CamelContext camelContext,
            ClassLoader classLoader, Object fsmEngineBuilder, PluginContext context)
            throws NoSuchMethodException, ClassNotFoundException {
        super(classLoader, classLoader.loadClass(context.getPluginClass()), fsmEngineBuilder, context.getMessagesSerializer());
        this.setCamelContext(camelContext);
        this.context = context;
    }

    @Override
    public void process(Exchange message) {
        final Exchange originalMessage = message.copy();
        final String resentId = (String) message.getIn().getHeader(MESSAGE_RESENT_ID_HEADER);
        if (resentId != null) {
            logger.debug("Handling previously resent message for plugin '{}' with id '{}'", context.getId(), resentId);
        }

        final String key = (String) message.getIn().getHeader(CORRELATION_KEY);
        if (isEmpty(key)) {
            logger.error("Empty keys are not allowed! SKIPPING MESSAGE for plugin '{}'", context.getId());
            return;
        }

        if (context.isShuttingDown()) {
            logger.warn("Context is shutting down, resending message for plugin '{}' with key '{}'",
                    context.getId(), key);
            resendWithDelay(originalMessage);
        }

        final AggregationRepository repo = context.getAggregationRepo();
        try {
            processOrDie(message, key, repo);
        } catch (RepositoryLockWaitException e) { //NOSONAR
            logger.warn("Unable to lock the entry, forcing unlock and resending message "
                            + "for plugin '{}' and key '{}', because of: {}",
                    context.getId(), key, e.getMessage());
            repo.confirm(camelContext, key);
            resendWithDelay(originalMessage);
        } catch (RepositoryUnreachableException | RepositoryNeedRestartException | RepositoryDirtyWriteAttemptException e) { //NOSONAR
            // resend with delay
            logger.warn("Repository is unreachable/dirty write, resending message "
                    + "for plugin '{}' with key '{}', because of: {}", context.getId(), key, e.getMessage());
            resendWithDelay(originalMessage);
        } catch (RepositoryFailureException e) {
            // skip message
            logger.error("Repository failure occurred, SKIPPING MESSAGE for plugin '{}' with key '{}'",
                    context.getId(), key, e);
        } catch (InvocationTargetException e) { //NOSONAR
            logger.error("Failed to aggregate, SKIPPING MESSAGE for plugin '{}' with key '{}': \n {}",
                    context.getId(), key, formatStackTrace(e.getTargetException()), e.getTargetException());
        } catch (Exception e) {
            logger.error("Failed to aggregate, SKIPPING MESSAGE for plugin '{}' with key '{}'",
                    context.getId(), key, e);
        } finally {
            unlockQuietly(repo, key);
        }
    }

    private void processOrDie(Exchange message, String key, AggregationRepository repo) throws Exception { //NOSONAR
        try {
            final Exchange state = repo.get(camelContext, key);
            final Exchange result = createCorrelatedCopy(super.aggregate(state, message), false);
            copyEmptyProperties(state, result);
            if (isCompleted(result)) {
                message.setIn(result.getIn());
                repo.remove(camelContext, key, result);
            } else {
                message.getIn().setBody(null);
                repo.add(camelContext, key, result);
            }
        } catch (Exception e) {
            message.getIn().setBody(null);
            throw e;
        }
    }

    private void copyEmptyProperties(Exchange state, Exchange result) {
        if (state != null) {
            for (String prop : state.getProperties().keySet()) {
                if (result.getProperty(prop) == null) {
                    result.setProperty(prop, state.getProperty(prop));
                }
            }
        }
    }

    private void resendWithDelay(Exchange message) {
        if (retryProducer == null) {
            retryProducer = camelContext.createProducerTemplate();
            retryProducer.setDefaultEndpointUri(context.getEndpoints().getDelayedInputUri());
        }
        message.getIn().setHeader(MESSAGE_RESENT_ID_HEADER, message.getExchangeId());
        retryProducer.send(message);
        logger.debug("Successfully resent message for plugin '{}' with id '{}'",
                context.getId(), message.getExchangeId());
    }

    private void unlockQuietly(AggregationRepository repo, String key) {
        if (repo instanceof AggregationRepositoryWithLocks) {
            ((AggregationRepositoryWithLocks) repo).unlockQuietly(key);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void injectFields(Object procInstance, Exchange exchange) {
        context.getInjector().inject(procInstance, context, exchange);
    }
}
