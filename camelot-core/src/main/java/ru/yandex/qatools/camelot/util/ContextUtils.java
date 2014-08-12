package ru.yandex.qatools.camelot.util;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ContextUtils {

    public static final AutowiredAnnotationBeanPostProcessor beanPostProcessor = new AutowiredAnnotationBeanPostProcessor();

    /**
     * Process the injection of the fields from the both Camel & Spring contexts
     */
    public static void autowireFields(Object bean, ApplicationContext applicationContext, CamelContext camelContext) throws Exception {
        if (applicationContext != null) {
            beanPostProcessor.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
            beanPostProcessor.processInjection(bean);
            if (bean instanceof ApplicationContextAware) {
                ((ApplicationContextAware) bean).setApplicationContext(applicationContext);
            }
        }
        if (camelContext != null) {
            DefaultCamelBeanPostProcessor processor = new DefaultCamelBeanPostProcessor(camelContext);
            processor.postProcessBeforeInitialization(bean, null);
            if (bean instanceof CamelContextAware) {
                ((CamelContextAware) bean).setCamelContext(camelContext);
            }
        }
    }
}
