package ru.yandex.qatools.camelot.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import ru.yandex.qatools.camelot.common.PluginsService;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class PluginsContextIntoBeansInjector implements ApplicationListener, ApplicationContextAware {
    protected final static Logger LOGGER = LoggerFactory.getLogger(PluginsContextIntoBeansInjector.class);

    private ApplicationContext context;
    private SpringContextInjector springInjector = new SpringContextInjector();
    private final PluginsService pluginsService;

    public PluginsContextIntoBeansInjector(PluginsService pluginsService) {
        this.pluginsService = pluginsService;
    }

    private Object performInjection(Object bean) {
        if (pluginsService != null) {
            springInjector.inject(bean, pluginsService, null);
        }
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    /**
     * Inject plugin context into all Spring beans after context is initialized completely
     */
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            ConfigurableListableBeanFactory clbf = ((AbstractApplicationContext) context).getBeanFactory();
            for (String name : context.getBeanDefinitionNames()) {
                final Object singleton = clbf.getSingleton(name);
                if(singleton != null) {
                    performInjection(singleton);
                }
            }
        }
    }
}
