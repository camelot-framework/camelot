package ru.yandex.qatools.camelot.api;

/**
 * The interface to use the global configuration options
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AppConfig {

    /**
     * Get string option
     */
    String getProperty(String key);

    /**
     * Get integer option
     */
    int getInt(String key);

    /**
     * Get long option
     */
    long getLong(String key);

    /**
     * Get double option
     */
    double getDouble(String key);

    /**
     * Get boolean option
     */
    boolean getBoolean(String key);
}
