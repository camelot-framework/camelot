package ru.yandex.qatools.camelot.web;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.Constants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

@Component
@Path("/ping")
@Scope("request")
public class PingResource {

    @Produce(uri = Constants.INPUT_QUEUE)
    private ProducerTemplate testProducer;

    @GET
    @Produces({MediaType.TEXT_PLAIN})
    public Response ping() {
        testProducer.sendBodyAndHeader(null, UUID, "ping");
        return Response.ok().build();
    }
}
