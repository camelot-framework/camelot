package ru.yandex.qatools.camelot.core.web;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.config.PluginContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class ViewHelperTest {

    @Autowired
    ViewHelper viewHelper;

    @Test
    public void testLifecycle() {
        assertEquals("lifecycle", viewHelper.renderPluginDashboard(getContext("lifecycle")));
        assertTrue(viewHelper.renderPluginWidgetContent(getContext("lifecycle")).contains("No such"));
    }

    @Test
    public void testAllSkipped() {
        assertEquals("all-skipped", viewHelper.renderPluginDashboard(getContext("all-skipped")));
        assertEquals(PluginViewHelper.class.getSimpleName(), viewHelper.renderPluginWidgetContent(getContext("all-skipped")));
    }

    @Test
    public void testTestStarted() {
        assertEquals("<p>Hello, World!</p>Hey", viewHelper.renderPluginDashboard(getContext("test-started")));
    }

    private PluginContext getContext(String pluginId) {
        return viewHelper.pluginsService.getPluginContext(pluginId);
    }
}
