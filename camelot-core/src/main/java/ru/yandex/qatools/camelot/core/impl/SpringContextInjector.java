package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.yandex.qatools.camelot.common.PluginContextInjectorImpl;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.PluginContext;

import static ru.yandex.qatools.camelot.util.ContextUtils.autowireFields;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SpringContextInjector extends PluginContextInjectorImpl
        implements CamelContextAware, ApplicationContextAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ApplicationContext applicationContext;
    private CamelContext camelContext;

    @Override
    public void inject(Object pluginObj, PluginsService service, PluginContext pluginConfig, Exchange exchange) {
        super.inject(pluginObj, service, pluginConfig, exchange);
        try {
            autowireFields(pluginObj, applicationContext, camelContext);
        } catch (Exception e) {
            logger.error("Could not autowire the fields for the object " + pluginObj, e);
        }
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
}
