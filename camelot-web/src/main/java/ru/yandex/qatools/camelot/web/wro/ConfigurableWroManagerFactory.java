package ru.yandex.qatools.camelot.web.wro;

import org.springframework.context.ApplicationContext;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ru.yandex.qatools.camelot.web.core.SpringServletFacade;

import javax.servlet.FilterConfig;
import java.util.Map;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ConfigurableWroManagerFactory extends ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory { //NOSONAR
    ApplicationContext applicationContext;
    SpringServletFacade facade;

    public void init(FilterConfig filterConfig){
        applicationContext = getWebApplicationContext(filterConfig.getServletContext());
        if (applicationContext != null) {
            facade = applicationContext.getBean(SpringServletFacade.class);
        }
    }

    @Override
    protected void contributeLocators(Map<String, UriLocator> map) {
        map.put(PluginsResourceLocator.ALIAS, new PluginsResourceLocator());
    }

    @Override
    protected void contributePreProcessors(Map<String, ResourcePreProcessor> map) {
        map.put(PluginsResourceCssUrlPreProcessor.ALIAS, new PluginsResourceCssUrlPreProcessor());
    }

    @Override
    protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
        CacheStrategy<CacheKey, CacheValue> cacheStrategy = super.newCacheStrategy();
        if (facade != null) {
            facade.setWroCacheStrategy(cacheStrategy);
        }
        return cacheStrategy;
    }

}
