package ru.yandex.qatools.camelot.test.core;

import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.core.impl.AppConfigSystemProperties;

import java.util.Properties;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class WrappedAppConfig extends AppConfigSystemProperties implements AppConfig {
    private final AppConfig baseConfig;
    private final Properties properties;

    public WrappedAppConfig(AppConfig baseConfig, Properties properties) {
        this.baseConfig = baseConfig;
        this.properties = properties;
    }

    @Override
    public String getProperty(String key) {
        return (properties.containsKey(key)) ? properties.getProperty(key) : baseConfig.getProperty(key);
    }

    public AppConfig getOriginal() {
        return baseConfig;
    }
}
