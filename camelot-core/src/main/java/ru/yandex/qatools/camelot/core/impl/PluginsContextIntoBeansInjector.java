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
import ru.yandex.qatools.camelot.config.PluginContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("unchecked")
public class PluginsContextIntoBeansInjector implements ApplicationListener, ApplicationContextAware {
    protected final static Logger LOGGER = LoggerFactory.getLogger(PluginsContextIntoBeansInjector.class);
    private final PluginsService pluginsService;
    private ApplicationContext context;
    private SpringContextInjector springInjector = new SpringContextInjector();

    public PluginsContextIntoBeansInjector(PluginsService pluginsService) {
        this.pluginsService = pluginsService;
    }

    private Object performInjection(Object bean) {
        if (pluginsService != null) {
            PluginContext plusginContext = null;
            try {
                plusginContext = pluginsService.getPlugin(bean.getClass()).getContext();
            } catch (Exception e) { //NOSONAR
                LOGGER.debug("Could not find plugin plusginContext for {}, using defaults", bean.getClass());
            }
            springInjector.inject(bean, pluginsService, plusginContext);
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
                if (singleton != null) {
                    performInjection(singleton);
                }
            }
        }
    }
}
