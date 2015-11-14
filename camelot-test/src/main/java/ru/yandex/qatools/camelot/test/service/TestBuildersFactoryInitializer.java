package ru.yandex.qatools.camelot.test.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.test.core.TestBuildersFactory;
import ru.yandex.qatools.camelot.test.core.TestContextInjector;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestBuildersFactoryInitializer implements ApplicationContextAware, BeanPostProcessor, PriorityOrdered {

    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PluginsService) {
            final PluginsService service = (PluginsService) bean;
            service.setBuildersFactory(new TestBuildersFactory(service.getBuildersFactory(), applicationContext));
            service.setContextInjector(new TestContextInjector(service.getContextInjector()));
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
