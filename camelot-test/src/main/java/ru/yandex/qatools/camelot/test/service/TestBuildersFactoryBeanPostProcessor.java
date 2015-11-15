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
public class TestBuildersFactoryBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, PriorityOrdered {

    // Not nice solution, but we'll have 10000 chances to overwrite the changes made by this bean later (if it's required)
    public static final int PRECEDENCE = Ordered.LOWEST_PRECEDENCE - 10000;

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
        return PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
