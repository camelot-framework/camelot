package ru.yandex.qatools.camelot.web;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.web.core.WebfrontEngine;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static jodd.util.MimeTypes.getMimeType;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.slf4j.LoggerFactory.getLogger;

@Component
@Scope("request")
@Path("/plugin/{plugin}/{resource:.*}")
public class PluginResourcesResource {
    private final Logger logger = getLogger(getClass());

    @Autowired
    WebfrontEngine pluginsService;

    @PathParam("resource")
    String resource;

    @PathParam("plugin")
    String pluginId;

    @GET
    @Produces({"image/*", "text/*"})
    public Response getResource() {
        try {
            final PluginContext context = pluginsService.getPluginContext(pluginId);
            final String resPath = context.getResDirPath() + resource;
            final String mimeType = getMimeType(getExtension(resPath));
            final InputStream res = pluginsService.getLoader().getResourceAsStream(context.getSource(), resPath);
            if (res != null) {
                return Response.ok(res, mimeType).build();
            }
            return Response.status(NOT_FOUND).build();
        } catch (Exception e) {
            logger.warn(String.format("Failed to find the resource %s for plugin %s", resource, pluginId), e);
            return Response.status(NOT_FOUND).build();
        }
    }

}
