package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.Thread.currentThread;
import static ru.yandex.qatools.camelot.api.Constants.Headers.FINISHED_EXCHANGE;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.ReflectUtil.invokeAnyMethod;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class FSMAggregationStrategy extends ClayProcessor implements AggregationStrategy {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    private final Class fsmClass;
    private Object fsmEngineBuilder;
    private Method buildWithoutStateMethod;
    private Method buildWithStateMethod;

    public FSMAggregationStrategy(Class fsmClass, MessagesSerializer serializer) throws NoSuchMethodException {
        super(fsmClass.getClassLoader(), serializer);
        initEngineBuilder(new CamelotFSMBuilder(fsmClass));
        this.fsmClass = fsmClass;
    }

    public FSMAggregationStrategy(ClassLoader classLoader, Class fsmClass, Object fsmEngineBuilder, MessagesSerializer serializer)
            throws NoSuchMethodException {
        super(classLoader, serializer);
        initEngineBuilder(fsmEngineBuilder);
        this.fsmClass = fsmClass;
    }

    @Override
    public Exchange aggregate(Exchange state, Exchange message) {
        Object result = state == null ? null : processAfterIn(state).getIn().getBody();
        final ClassLoader originalCL = currentThread().getContextClassLoader();

        Object fsmEngine;

        try {
            currentThread().setContextClassLoader(classLoader);
            message = processAfterIn(message); //NOSONAR

            Object fsm = fsmClass.newInstance();
            injectFields(fsm, message);

            if (result != null) {
                fsmEngine = buildWithStateMethod.invoke(fsmEngineBuilder, result, fsm);
            } else {
                fsmEngine = buildWithoutStateMethod.invoke(fsmEngineBuilder, fsm);
            }

            Object event = message.getIn().getBody();

            try {
                result = invokeAnyMethod(fsmEngine, "fire", new Class[]{Object.class}, event);
            } catch (InvocationTargetException e) { //NOSONAR
                logger.error("Failed to process message {} with FSM {}! \n {}",
                                event, fsm, formatStackTrace(e.getTargetException()), e.getTargetException());
            } catch (Exception e) {
                logger.error("Failed to process message {} with FSM {}! \n {}",
                        event, fsm, formatStackTrace(e), e);
            }

            message.getIn().setHeader(FINISHED_EXCHANGE, invokeAnyMethod(fsmEngine, "isCompleted"));

        } catch (Exception e) {
            logger.error("{}: \n {}", fsmEngineBuilder, formatStackTrace(e), e);
        } finally {
            if (!message.getIn().getHeaders().containsKey(FINISHED_EXCHANGE)) {
                message.getIn().setHeader(FINISHED_EXCHANGE, false);
            }
            currentThread().setContextClassLoader(originalCL);
        }

        message.getIn().setBody(result);
        return processBeforeOut(message);
    }

    private void initEngineBuilder(Object fsmEngineBuilder) throws NoSuchMethodException {
        this.fsmEngineBuilder = fsmEngineBuilder;
        final Class<?> builderClass = fsmEngineBuilder.getClass();
        this.buildWithStateMethod = builderClass.getMethod("build", Object.class, Object.class);
        this.buildWithStateMethod.setAccessible(true);
        this.buildWithoutStateMethod = builderClass.getMethod("build", Object.class);
        this.buildWithoutStateMethod.setAccessible(true);
    }

    @SuppressWarnings("suspicious")
    public boolean isCompleted(Exchange exchange) throws ReflectiveOperationException {
        return exchange == null
                || exchange.getIn() == null
                || (boolean) exchange.getIn().removeHeader(FINISHED_EXCHANGE);
    }
}
