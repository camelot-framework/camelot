package ru.yandex.qatools.camelot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.camelot.core.beans.StopAllSkipped;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedService;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.core.util.TestEventGenerator.createTestSkipped;
import static ru.yandex.qatools.camelot.core.util.TestEventsUtils.copyOf;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginResourceBeanName;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class ResourceTest extends BasicAggregatorsTest {
    @Autowired
    ApplicationContext applicationContext;

    @Test
    public void testResourceIsAdded() throws Exception {
        final Object bean = applicationContext.getBean(pluginResourceBeanName("all-skipped"));
        assertNotNull("Resouce must be added to the context", bean);
        assertTrue("Resouce must be added to the context", bean instanceof AllSkippedService);
        final AllSkippedService allSkipped = (AllSkippedService) bean;
        assertNotNull("Resouce must contain producer", allSkipped.getProducer());
        assertNotNull("Resouce must contain repository", allSkipped.getRepository());
        assertThat(allSkipped.getCounterRepo(), not(sameInstance(allSkipped.getRepository())));
    }

    @Test
    public void testResourceEndpointListener() throws Exception {
        final AllSkippedService service = (AllSkippedService) applicationContext.getBean(pluginResourceBeanName("all-skipped"));
        assertNotNull("Resouce must contain endpoint listener ", service.getListener());
        new Thread() {
            @Override
            public void run() {
                sendTestEvent("all-skipped", createTestSkipped());
                sendStopEvent("all-skipped", copyOf(createTestSkipped(), StopAllSkipped.class));
            }
        }.start();
        assertTrue("Result must be true", service.waitAllSkipped());
    }

}
