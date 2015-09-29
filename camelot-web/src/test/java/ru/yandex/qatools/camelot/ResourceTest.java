package ru.yandex.qatools.camelot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedService;
import ru.yandex.qatools.camelot.core.plugins.ResourceOnlyService;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginResourceBeanName;
import static ru.yandex.qatools.camelot.util.TestEventGenerator.createTestSkipped;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class ResourceTest extends ActivemqAggregatorsTest {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void testResourceWithoutPluginClass() throws Exception {
        final Object bean = applicationContext.getBean(pluginResourceBeanName("resource-only"));
        assertNotNull("Resource must be added to the context", bean);
        assertTrue("Resource must be added to the context", bean instanceof ResourceOnlyService);
        final ResourceOnlyService resOnly = (ResourceOnlyService) bean;
        assertNull("Resource must not contain producer", resOnly.getProducer());
        assertNotNull("Resource must contain main producer", resOnly.getMainProducer());
        assertNull("Resource must not contain repository", resOnly.getRepository());
        assertNotNull("Resource must contain foreign repository", resOnly.getCounterRepo());
        resOnly.getMainProducer().produce(createTestSkipped()); // test can produce to main queue (no error)
    }

    @Test
    public void testResourceIsAdded() throws Exception {
        final Object bean = applicationContext.getBean(pluginResourceBeanName("all-skipped"));
        assertNotNull("Resource must be added to the context", bean);
        assertTrue("Resource must be added to the context", bean instanceof AllSkippedService);
        final AllSkippedService allSkipped = (AllSkippedService) bean;
        assertNotNull("Resource must contain producer", allSkipped.getProducer());
        assertNotNull("Resource must contain repository", allSkipped.getRepository());
        assertThat(allSkipped.getCounterRepo(), not(sameInstance(allSkipped.getRepository())));
    }
}
