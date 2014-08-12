package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AppConfigSpringProperties extends AppConfigSystemProperties {

    @Override
    public String getProperty(String key) {
        String value = (String) ListablePropertyPlaceholderConfigurer.getProperties().get(key);
        return (value != null) ? value : super.getProperty(key);
    }
}
