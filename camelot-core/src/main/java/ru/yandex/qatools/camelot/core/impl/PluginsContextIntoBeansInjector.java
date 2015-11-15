package ru.yandex.qatools.camelot.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import ru.yandex.qatools.camelot.common.ProcessingEngine;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class PluginsContextIntoBeansInjector implements BeanPostProcessor, Ordered {
    public static final int PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 50000;
    protected final static Logger LOGGER = LoggerFactory.getLogger(PluginsContextIntoBeansInjector.class);

    @Autowired
    private ProcessingEngine processingEngine;
    private SpringContextInjector springInjector = new SpringContextInjector();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return performInjection(bean);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return performInjection(bean);
    }

    @Override
    public int getOrder() {
        return PRECEDENCE;
    }

    private Object performInjection(Object bean) {
        if (processingEngine != null) {
            springInjector.inject(bean, processingEngine, null);
        }
        return bean;
    }
}
