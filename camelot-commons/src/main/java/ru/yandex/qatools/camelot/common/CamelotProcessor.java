package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.error.CallException;
import ru.yandex.qatools.camelot.error.DispatchException;
import ru.yandex.qatools.fsm.annotations.OnException;

import java.lang.reflect.Method;
import java.util.Map;

import static ru.yandex.qatools.camelot.common.Metadata.getMeta;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotProcessor extends PluggableProcessor {
    final protected PluginContext context;
    final protected Class procClass;

    public CamelotProcessor(ClassLoader classLoader, Class procClass, PluginContext context) {
        super(classLoader, procClass, context.getMessagesSerializer());
        this.context = context;
        this.procClass = procClass;
    }

    public CamelotProcessor(ClassLoader classLoader, Class procClass, Object processor, PluginContext context) {
        super(classLoader, procClass, processor, context.getMessagesSerializer());
        this.context = context;
        this.procClass = procClass;
    }

    @Override
    public void process(Exchange message) {
        if (context.isShuttingDown()) {
            logger.warn("Message is not going to be processed due to context shutting down: {}", message.getIn().getBody());
            return;
        }
        super.process(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void injectFields(Object procInstance, Exchange exchange) {
        context.getInjector().inject(procInstance, context, exchange);
    }

    @Override
    public Object dispatchMessage(Object processor, Object event, Map<String, Object> headers) throws DispatchException {
        try {
            return super.dispatchMessage(processor, event, headers);
        } catch (DispatchException e) {
            final AnnotatedMethodDispatcher caller = new AnnotatedMethodDispatcher(processor, getMeta(procClass));
            final Map<Method, Object> called;
            try {
                final Throwable cause = (e.getCause() instanceof CallException) ? e.getCause().getCause() : e.getCause();
                called = caller.dispatch(OnException.class, true, cause, event);
                if (!called.isEmpty() && called.size() == 1) {
                    Method m = called.keySet().iterator().next();
                    Object result = called.values().iterator().next();
                    if (m.getAnnotation(OnException.class).preserve()) {
                        return result;
                    }
                }
                throw e;
            } catch (Exception failbackE) {
                logger.error("Failed to invoke @OnException on processor {} ", processor, failbackE);
            }
            logger.warn("Failed to dispatch message {} using processor {}: {}! \n {}",
                    event, processor, e.getMessage(), formatStackTrace(e), e);
        }
        return null;
    }
}
