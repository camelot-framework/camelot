package ru.yandex.qatools.camelot.web;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Path("/ping")
public class PingResource {

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response ping() {
        return Response.ok().build();
    }
}
