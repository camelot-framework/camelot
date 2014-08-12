package ru.yandex.qatools.camelot.spring;

import org.junit.Test;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer.getProperties;
import static ru.yandex.qatools.camelot.util.ReflectUtil.resolveResourcesFromPattern;

public class ListablePropertyPlaceholderConfigurerTest {

    @Test
    public void testPropertiesLoad() throws IOException, ClassNotFoundException {
        ListablePropertyPlaceholderConfigurer configurer = new ListablePropertyPlaceholderConfigurer();
        Collection<Resource> resources = resolveResourcesFromPattern("classpath:test-config.properties");
        configurer.setLocations(resources.toArray(new Resource[resources.size()]));

        assertEquals("test", getProperties().getProperty("custom.plugin.stringValue"));
    }
}
