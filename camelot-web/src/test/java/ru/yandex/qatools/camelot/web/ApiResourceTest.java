package ru.yandex.qatools.camelot.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
public class ApiResourceTest {

    @Autowired
    ApiResource apiResource;

    @Test
    public void testRender() throws IOException {
        final ServletContext servletContext = mock(ServletContext.class);
        final HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletContext.getContextPath()).thenReturn("/");
        when(servletRequest.getRequestURI()).thenReturn("/");
        String apiView = apiResource.render(servletContext, servletRequest);
        assertThat(apiView, containsString("API"));
        assertThat(apiView, containsString(ApiResource.class.getName()));
        assertThat(apiView, not(containsString("null")));
    }
}
