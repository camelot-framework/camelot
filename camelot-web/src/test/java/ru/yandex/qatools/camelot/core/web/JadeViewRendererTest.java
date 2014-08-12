package ru.yandex.qatools.camelot.core.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.camelot.web.ApiResource;
import ru.yandex.qatools.camelot.web.ViewRenderer;

import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class JadeViewRendererTest {

    @Autowired
    ViewRenderer renderer;

    @Test
    public void testRender() throws IOException {
        String testView = renderer.renderWithDefaultLayout("test-view.jade", map("body", (Object) "testValue"));
        assertThat(testView, containsString("Body:view testValue"));
        assertThat(testView, containsString("subsubsub"));

        String apiResource = renderer.renderWithDefaultLayout(ApiResource.class);
        assertThat(apiResource, containsString("API"));

        String errorView = renderer.renderWithDefaultLayout("error.jade", new HashMap<String, Object>());
        assertThat(errorView, containsString("Server error"));
    }
}
