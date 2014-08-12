package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.AppConfig;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AppConfigSystemProperties implements AppConfig {

    @Override
    public String getProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public int getInt(String key) {
        return Integer.valueOf(getProperty(key));
    }

    @Override
    public long getLong(String key) {
        return Long.valueOf(getProperty(key));
    }

    @Override
    public double getDouble(String key) {
        return Double.valueOf(getProperty(key));
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.valueOf(getProperty(key));
    }
}
