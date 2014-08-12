package ru.yandex.qatools.camelot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.WebfrontEngine;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component
@Path("/plugins")
public class PluginsResource {

    @Autowired
    ProcessingEngine processingEngine;
    @Autowired
    WebfrontEngine webfrontEngine;

    @POST
    @Path("/reload")
    public Response reloadPlugins() {
        webfrontEngine.stop();
        processingEngine.stop();
        processingEngine.reloadAndStart();
        webfrontEngine.reloadAndStart();
        return Response.ok().build();
    }
}
