package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.core.MessagesSerializer;
import ru.yandex.qatools.camelot.error.DispatchException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.core.impl.Metadata.getMeta;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluggableProcessor extends ClayProcessor implements Processor {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    final private Class procClass;
    final private Object processor;

    public PluggableProcessor(ClassLoader classLoader, Class procClass, MessagesSerializer messagesSerializer) {
        super(classLoader, messagesSerializer);
        this.procClass = procClass;
        this.processor = null;
    }

    public PluggableProcessor(ClassLoader classLoader, Class procClass, Object processor, MessagesSerializer messagesSerializer) {
        super(classLoader, messagesSerializer);
        this.procClass = procClass;
        this.processor = processor;
    }

    public PluggableProcessor(Class procClass, MessagesSerializer messagesSerializer) {
        this(procClass.getClassLoader(), procClass, messagesSerializer);
    }

    public PluggableProcessor(Class procClass, Object processor, MessagesSerializer messagesSerializer) {
        super(procClass.getClassLoader(), messagesSerializer);
        this.procClass = procClass;
        this.processor = processor;
    }

    @Override
    public void process(Exchange message) {
        final ClassLoader originalCL = Thread.currentThread().getContextClassLoader();
        Object result = null;

        try {
            processAfterIn(message);
            Object processor = (this.processor != null) ? this.processor : procClass.newInstance();
            Thread.currentThread().setContextClassLoader(classLoader);
            Object event = message.getIn().getBody();
            injectFields(processor, message);
            if (event != null) {
                result = dispatchMessage(processor, event, message.getIn().getHeaders());
            }
        } catch (Exception e) {
            logger.error(procClass + ": \n " + formatStackTrace(e), e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalCL);
        }
        message.getIn().setBody(result);
        processBeforeOut(message);
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
