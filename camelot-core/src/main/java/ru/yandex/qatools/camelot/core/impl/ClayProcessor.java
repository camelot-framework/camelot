package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.api.annotations.InjectHeaders;
import ru.yandex.qatools.camelot.util.ContextUtils;

import java.io.Serializable;
import java.lang.reflect.Field;

import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ClayProcessor implements CamelContextAware, ApplicationContextAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    boolean serializeMessages = false;
    protected ClassLoader classLoader;
    protected ApplicationContext applicationContext;
    protected CamelContext camelContext;

    protected ClayProcessor(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Set the classloader which is used to serialize/deserialize & load objects from input exchange
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Returns the currently set classloader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    public boolean isSerializeMessages() {
        return serializeMessages;
    }

    public void setSerializeMessages(boolean serializeMessages) {
        this.serializeMessages = serializeMessages;
    }

    @SuppressWarnings("unchecked")
    protected Object processAfterIn(Object event, String bodyClass) {
        if (event instanceof byte[] && serializeMessages) {
            try {
                event = deserializeFromBytes((byte[]) event, classLoader, (Class<? extends Serializable>) classLoader.loadClass(bodyClass));
            } catch (Exception e) {
                logger.debug("Failed to deserialize message from bytes: " + e.getMessage(), e);
            }
        }
        return event;
    }

    protected Object processBeforeOut(Object result) {
        return (serializeMessages) ? serializeToBytes(result, classLoader) : result;
    }

    protected void injectFields(Object procInstance, Exchange exchange) {
        try {
            ContextUtils.autowireFields(procInstance, applicationContext, camelContext);
        } catch (Exception e) {
            logger.error("Could not autowire the Spring or Camel context fields: ", e);
        }
        for (Field field : getFieldsInClassHierarchy(procInstance.getClass())) {
            try {
                boolean oldAccessible = field.isAccessible();
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                if (getAnnotation(field, InjectHeader.class) != null) {
                    String headerName = (String) getAnnotationValue(field, InjectHeader.class, "value");
                    field.set(procInstance, exchange.getIn().getHeader(headerName));
                }
                if (getAnnotation(field, InjectHeaders.class) != null) {
                    field.set(procInstance, exchange.getIn().getHeaders());
                }
                field.setAccessible(oldAccessible);
            } catch (Exception e) {
                logger.error("Inject field " + field.getName() + " of FSM " + procInstance + " error: ", e);
            }
        }
    }

}
