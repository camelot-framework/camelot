package ru.yandex.qatools.camelot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public abstract class BasicViewResource {
    protected final static String APP_JSON = MediaType.APPLICATION_JSON + ";charset=utf-8";

    @Autowired
    ViewRenderer viewRenderer;

    @GET
    @Path("/")
    public String render(@Context ServletContext context, @Context HttpServletRequest request) throws IOException {
        return viewRenderer.renderWithDefaultLayout(getClass(),
                map(
                        "this", (Object) this,
                        "title", getTitle(),
                        "request", request,
                        "req_path", request.getRequestURI().replace(context.getContextPath(), "")
                ));
    }

    public Object getTitle() {
        return "Camelot";
    }
}
