package ru.yandex.qatools.camelot.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Constants;
import org.springframework.core.io.Resource;

import java.util.Properties;

import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.copyOf;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static ru.yandex.qatools.camelot.util.IOUtils.readResource;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ListablePropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private static final Constants CONSTANTS = new Constants(PropertyPlaceholderConfigurer.class);
    protected final static Properties properties = new Properties();
    private int springSystemPropertiesMode = SYSTEM_PROPERTIES_MODE_OVERRIDE;
    private boolean ignoreResourceNotFound = false;
    private Resource[] locations;

    @Override
    public void setLocations(Resource[] locations) {
        super.setLocations(locations);
        this.locations = copyOf(locations, locations.length);
    }

    @Override
    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        super.setIgnoreResourceNotFound(ignoreResourceNotFound);
        this.ignoreResourceNotFound = ignoreResourceNotFound;
    }


    @Override
    public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
        super.setSystemPropertiesModeName(constantName);
        springSystemPropertiesMode = CONSTANTS.asNumber(constantName).intValue();
    }

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        if (localProperties != null) {
            for (Properties localProps : localProperties) {
                props.putAll(localProps);
            }
        }
        super.processProperties(beanFactory, props);
        if (locations != null) {
            for (Resource resource : locations) {
                try {
                    final String url = resource.getURL().toString();
                    logger.info(format("Trying to load properties from %s", url));
                    final Properties loader = new Properties();
                    loader.load(readResource(resource.getURL()));
                    getProperties().putAll(loader);
                } catch (Exception e) {
                    if (!ignoreResourceNotFound) {
                        logger.error(format("Failed to load properties from location %s current working dir: %s",
                                resource, getProperty("user.dir")), e);
                    } else {
                        logger.warn(format("Ignoring not existing resource %s (%s)", resource, e.getMessage()));
                    }
                }
            }
        }
        for (Object key : properties.stringPropertyNames()) {
            String keyStr = key.toString();
            String valueStr = resolvePlaceholder(keyStr, props, springSystemPropertiesMode);
            if (!isBlank(valueStr)) {
                properties.put(keyStr, valueStr);
                logger.info(format("Initial property: %s -> %s", key, valueStr));
            }
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
