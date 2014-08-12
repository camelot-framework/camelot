package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.FINISHED_EXCHANGE;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.ReflectUtil.invokeAnyMethod;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class FSMAggregationStrategy extends ClayProcessor implements AggregationStrategy {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    private Object fsmEngineBuilder;
    private Method buildWithoutStateMethod;
    private Method buildWithStateMethod;
    private final Class fsmClass;

    public FSMAggregationStrategy(Class fsmClass) throws NoSuchMethodException {
        super(fsmClass.getClassLoader());
        initEngineBuilder(new CamelotFSMBuilder(fsmClass));
        this.fsmClass = fsmClass;
    }

    public FSMAggregationStrategy(ClassLoader classLoader, Class fsmClass, Object fsmEngineBuilder) throws NoSuchMethodException {
        super(classLoader);
        initEngineBuilder(fsmEngineBuilder);
        this.fsmClass = fsmClass;
    }

    @Override
    public Exchange aggregate(Exchange state, Exchange message) {
        Object result = state == null ? null : state.getIn().getBody();
        final ClassLoader originalCL = Thread.currentThread().getContextClassLoader();

        Object fsmEngine;

        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            Object fsm = fsmClass.newInstance();
            injectFields(fsm, message);

            String eventBodyClass = (String) message.getIn().getHeader(BODY_CLASS);
            String stateBodyClass = (state != null) ? (String) state.getIn().getHeader(BODY_CLASS) : null;
            logger.debug(format("%s's input, stateBodyClass=%s, eventBodyClass=%s", fsmClass, stateBodyClass, eventBodyClass));

            if (result != null) {
                result = processAfterIn(result, stateBodyClass);
                fsmEngine = buildWithStateMethod.invoke(fsmEngineBuilder, result, fsm);
            } else {
                fsmEngine = buildWithoutStateMethod.invoke(fsmEngineBuilder, fsm);
            }

            if (eventBodyClass == null) {
                logger.warn(format("%s got message without bodyClass header, skipping aggregation!", fsmClass));
            } else {
                Object event = processAfterIn(message.getIn().getBody(), eventBodyClass);

                try {
                    result = invokeAnyMethod(fsmEngine, "fire", new Class[]{Object.class}, event);
                } catch (InvocationTargetException e) {
                    logger.trace("Sonar trick", e);
                    logger.error(format("Failed to process message %s with FSM %s! \n %s", event, fsm,
                            formatStackTrace(e.getTargetException())), e.getTargetException());
                } catch (Exception e) {
                    logger.error(format("Failed to process message %s with FSM %s! \n %s", event, fsm,
                            formatStackTrace(e)), e);
                }
                if (result != null) {
                    final String newBodyClass = result.getClass().getName();
                    logger.debug(format("%s's output is not null, bodyClass=%s", fsmClass, newBodyClass));
                    message.getIn().setHeader(BODY_CLASS, newBodyClass);
                }
                result = processBeforeOut(result);

                if ((Boolean) invokeAnyMethod(fsmEngine, "isCompleted")) {
                    message.getIn().setHeader(FINISHED_EXCHANGE, true);
                }
            }
        } catch (Exception e) {
            logger.error(fsmEngineBuilder + ": \n " + formatStackTrace(e), e);
        } finally {
            if (!message.getIn().getHeaders().containsKey(FINISHED_EXCHANGE)) {
                message.getIn().setHeader(FINISHED_EXCHANGE, false);
            }
            Thread.currentThread().setContextClassLoader(originalCL);
        }

        if (result != null) {
            message.getIn().setBody(result);
        }
        return message;
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
    public boolean isCompleted(Exchange exchange) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return exchange == null
                || exchange.getIn() == null
                || (boolean) exchange.getIn().getHeader(FINISHED_EXCHANGE);
    }
}
