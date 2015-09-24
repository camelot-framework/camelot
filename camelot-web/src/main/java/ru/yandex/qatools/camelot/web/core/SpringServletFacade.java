package ru.yandex.qatools.camelot.web.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ru.yandex.qatools.camelot.spring.ServletContainer;

/**
 * TODO: this class is a hack to inject some important things into the spring context
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SpringServletFacade {
    final Logger logger = LoggerFactory.getLogger(getClass());
    private ServletContainer servletContainer;
    private CacheStrategy<CacheKey, CacheValue> wroCacheStrategy;

    public void setServletContainer(ServletContainer servletContainer) {
        logger.info("setting the spring servlet:" + servletContainer);
        this.servletContainer = servletContainer;
    }

    public void setWroCacheStrategy(CacheStrategy<CacheKey, CacheValue> wroCacheStrategy) {
        logger.info("setting the wro cache strategy:" + wroCacheStrategy);
        this.wroCacheStrategy = wroCacheStrategy;
    }

    public void restartQuietly() {
        try {
            reloadServlet();
            clearCache();
        } catch (Exception e) {
            logger.warn("Failed to restart web services: ", e);
        }
    }

    public void clearCache() throws Exception { //NOSONAR
        if (wroCacheStrategy != null) {
            wroCacheStrategy.clear();
        } else {
            logger.warn("Wro cache strategy is null! No need to clear the cache...");
        }
    }

    public void reloadServlet() throws Exception { //NOSONAR
        if (servletContainer == null) {
            throw new RuntimeException("Spring initialization failure: SpringServlet is null!"); //NOSONAR
        }
        servletContainer.destroy();
        servletContainer.init();
    }
}
