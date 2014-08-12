package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.error.DispatchException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.core.impl.Metadata.getMeta;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluggableProcessor extends ClayProcessor implements Processor {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    final private Class procClass;
    final private Object processor;

    public PluggableProcessor(ClassLoader classLoader, Class procClass) {
        super(classLoader);
        setSerializeMessages(true);
        this.procClass = procClass;
        this.processor = null;
    }

    public PluggableProcessor(ClassLoader classLoader, Class procClass, Object processor) {
        super(classLoader);
        setSerializeMessages(true);
        this.procClass = procClass;
        this.processor = processor;
    }

    public PluggableProcessor(Class procClass) {
        this(procClass.getClassLoader(), procClass);
        setSerializeMessages(false);
    }

    public PluggableProcessor(Class procClass, Object processor) {
        super(procClass.getClassLoader());
        setSerializeMessages(false);
        this.procClass = procClass;
        this.processor = processor;
    }

    @Override
    public void process(Exchange message) {
        final ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
        Object result = null;

        try {
            Object processor = (this.processor != null) ? this.processor : procClass.newInstance();
            Thread.currentThread().setContextClassLoader(classLoader);
            String oldBodyClass = (String) message.getIn().getHeader(BODY_CLASS);
            Object event = message.getIn().getBody();
            logger.debug(format("%s input bodyClass=%s", procClass, oldBodyClass));
            event = processAfterIn(event, oldBodyClass);
            injectFields(processor, message);
            if (event != null) {
                result = dispatchMessage(processor, event, message.getIn().getHeaders());
                if (result != null) {
                    final String newBodyClass = result.getClass().getName();
                    logger.debug(format("%s's out is not null, bodyClass=%s", procClass, newBodyClass));
                    message.getIn().setHeader(BODY_CLASS, newBodyClass);
                }
                result = processBeforeOut(result);
            }
        } catch (Exception e) {
            logger.error(procClass + ": \n " + formatStackTrace(e), e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalCL);
        }
        message.getIn().setBody(result);
    }

    protected Object dispatchMessage(Object processor, Object event, Map<String, Object> headers) throws DispatchException {
        final AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(processor, getMeta(procClass));
        try {
            Map<Method, Object> res = caller.dispatch(ru.yandex.qatools.camelot.api.annotations.Processor.class, true, event, headers);
            if (res.isEmpty()) {
                throw new DispatchException(format("Could not find the suitable @Processor method within processor %s",
                        processor));
            }
            return res.values().iterator().next();
        } catch (InvocationTargetException e) {
            logger.trace("Sonar cheat", e);
            throw new DispatchException(format("Failed to call @Processor method: %s!",
                    e.getTargetException().getMessage()), e.getTargetException());
        } catch (Exception e) {
            throw new DispatchException(format("Failed to call @Processor method: %s!", e.getMessage()), e);
        }
    }

}
