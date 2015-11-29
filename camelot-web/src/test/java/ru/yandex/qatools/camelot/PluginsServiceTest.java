package ru.yandex.qatools.camelot;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.web.core.WebfrontEngine;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedService;
import ru.yandex.qatools.camelot.core.plugins.LifecycleFSM;
import ru.yandex.qatools.camelot.core.plugins.TestStartedCounterAggregator;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class PluginsServiceTest {

    @Autowired
    WebfrontEngine pluginsService;

    @Test
    public void testConfig() {
        final PluginContext conf = pluginsService.getPluginContext("all-skipped");
        final String basePath = AllSkippedService.class.getName().replaceAll("\\.", "/") + "/";
        assertEquals(basePath, conf.getResDirPath());
        assertEquals(basePath + "dashboard.html", conf.getDashboardPath());
        assertThat(conf.getCssPaths(), containsInAnyOrder(
                basePath + "styles.css",
                basePath + "more-styles.less"
        ));
        assertThat(conf.getJsPaths(), containsInAnyOrder(
                basePath + "script.js",
                basePath + "script.coffee",
                basePath + "script2.js"
        ));
        assertEquals(basePath + "widget.jade", conf.getWidgetPath());

        assertNotNull(conf.getClassLoader());
        assertNotNull(conf.getInput());
        assertNotNull(conf.getOutput());
        assertNotNull(conf.getStorage());
        assertNotNull(conf.getRepository());
        assertEquals("/allSkipped", conf.getResPathMapping());
    }

    @Test
    public void testHasResourceButInAggregator() {
        final PluginContext conf = pluginsService.getPluginContext("lifecycle");
        final String basePath = LifecycleFSM.class.getName().replaceAll("\\.", "/") + "/";
        assertNotNull(conf.getResDirPath());
        assertNotNull(conf.getResPathMapping());
        assertEquals(basePath + "dashboard.mustache", conf.getDashboardPath());
    }

    @Test
    public void testResourcesOnlyInAggregator() {
        final PluginContext conf = pluginsService.getPluginContext("test-started");
        final String basePath = TestStartedCounterAggregator.class.getName().replaceAll("\\.", "/") + "/";
        assertEquals(basePath, conf.getResDirPath());
        assertEquals(basePath + "dashboard.jade", conf.getDashboardPath());

        assertNotNull(conf.getClassLoader());
        assertNotNull(conf.getInput());
        assertNotNull(conf.getOutput());
        assertNotNull(conf.getStorage());
        assertNotNull(conf.getRepository());
        assertNotNull(conf.getResDirPath());
        assertNull(conf.getResPathMapping());
    }

    @Test(expected = PluginsSystemException.class)
    public void getNonExistingPlugin() {
        pluginsService.getPlugin("non-existing-plugin");
    }
}
