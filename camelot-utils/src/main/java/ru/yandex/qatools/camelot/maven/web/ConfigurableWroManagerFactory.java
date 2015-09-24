package ru.yandex.qatools.camelot.maven.web;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.cache.CacheStrategy;
import ro.isdc.wro.cache.CacheValue;
import ro.isdc.wro.config.Context;

import javax.servlet.FilterConfig;
import java.util.Properties;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ConfigurableWroManagerFactory extends ru.yandex.qatools.camelot.web.wro.ConfigurableWroManagerFactory { //NOSONAR
    private Properties configProperties;

    @Override
    protected CacheStrategy<CacheKey, CacheValue> newCacheStrategy() {
        final ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(Context.get().getServletContext());
        return new ConfigurableCacheStrategy(context) {
            @Override
            protected Properties newProperties() {
                final Properties props = new Properties();
                updatePropertiesWithConfiguration(props, ro.isdc.wro.cache.ConfigurableCacheStrategy.KEY);
                return props;
            }
        };
    }

    /**
     * Add to properties a new key with value extracted either from filterConfig or from configurable properties file.
     * This method helps to ensure backward compatibility of the filterConfig vs configProperties configuration.
     *
     * @param props
     *          the {@link java.util.Properties} which will be populated with the value extracted from filterConfig or
     *          configProperties for the provided key.
     * @param key
     *          to read from filterConfig or configProperties and put into props.
     */
    private void updatePropertiesWithConfiguration(final Properties props, final String key) {
        final FilterConfig filterConfig = Context.get().getFilterConfig();
        // first, retrieve value from init-param for backward compatibility
        final String valuesAsString = filterConfig.getInitParameter(key);
        if (valuesAsString != null) {
            props.setProperty(key, valuesAsString);
        } else {
            // retrieve value from configProperties file
            final String value = getConfigProperties().getProperty(key);
            if (value != null) {
                props.setProperty(key, value);
            }
        }
    }


    /**
     * Use this method rather than accessing the field directly, because it will create a default one if none is provided.
     */
    private Properties getConfigProperties() {
        if (configProperties == null) {
            configProperties = newConfigProperties();
        }
        return configProperties;
    }
}
