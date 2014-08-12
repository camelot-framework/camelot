package ru.yandex.qatools.camelot.core.web;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

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
        assertEquals("lifecycle", viewHelper.renderPluginDashboard("lifecycle"));
        assertTrue(viewHelper.renderPluginWidgetContent("lifecycle").contains("No such"));
    }

    @Test
    public void testAllSkipped() {
        assertEquals("all-skipped", viewHelper.renderPluginDashboard("all-skipped"));
        assertEquals(PluginViewHelper.class.getSimpleName(), viewHelper.renderPluginWidgetContent("all-skipped"));
    }

    @Test
    public void testTestStarted() {
        assertEquals("<p>Hello, World!</p>Hey", viewHelper.renderPluginDashboard("test-started"));
    }
}
