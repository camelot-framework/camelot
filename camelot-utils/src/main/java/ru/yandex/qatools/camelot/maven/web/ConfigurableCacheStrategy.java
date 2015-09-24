package ru.yandex.qatools.camelot.maven.web;

import org.springframework.context.ApplicationContext;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheValue;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ConfigurableCacheStrategy extends ro.isdc.wro.cache.ConfigurableCacheStrategy { //NOSONAR

    final ApplicationContext context;

    public ConfigurableCacheStrategy(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void put(CacheKey key, CacheValue value) {
        if (!"plugins".equals(key.getGroupName())) {
            super.put(key, value);
        }
    }
}
