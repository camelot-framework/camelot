package ru.yandex.qatools.camelot.spring;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.spring.ListablePropertyPlaceholderConfigurer.getProperties;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:camelot-core-context.xml", "classpath*:test-camelot-core-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class ListablePropertyPlaceholderConfigurerTest {

    @Test
    public void testPropertiesLoad() throws IOException, ClassNotFoundException {
        assertEquals("test", getProperties().getProperty("custom.plugin.stringValue"));
    }
}
