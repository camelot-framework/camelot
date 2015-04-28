package ru.yandex.qatools.camelot.web;

import org.apache.camel.CamelContext;
import org.jboss.logging.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.Constants;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.beans.InputEvent;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static ru.yandex.qatools.camelot.util.ServiceUtil.initEventProducer;

@Component
@Path("/events")
public class InputResource {

    private final EventProducer input;

    @Autowired
    public InputResource(CamelContext context) throws Exception {
        input = initEventProducer(context, Constants.INPUT_QUEUE);
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML})
    public Response sendMessage(@Param InputEvent event) {
        input.produce(event.getEvent());
        return Response.ok().build();
    }

}
