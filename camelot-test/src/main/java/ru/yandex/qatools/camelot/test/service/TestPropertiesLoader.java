package ru.yandex.qatools.camelot.test.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer;

import java.net.URL;
import java.util.Properties;

import static ru.yandex.qatools.camelot.util.IOUtils.readResource;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestPropertiesLoader extends ListablePropertyPlaceholderConfigurer {
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {
        super.processProperties(beanFactory, props);
    }

    public void appendPropertiesFile(URL path) throws Exception {
        Properties propFile = new Properties();
        propFile.load(readResource(path));
        properties.putAll(propFile);
    }
}
