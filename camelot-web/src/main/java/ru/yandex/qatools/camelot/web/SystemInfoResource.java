package ru.yandex.qatools.camelot.web;

import org.apache.camel.CamelContext;
import org.apache.camel.component.seda.SedaComponent;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

@Component
@Path("/system")
@Scope("request")
public class SystemInfoResource {

    @Inject
    CamelContext camelContext;

    @GET
    @Path("seda/queues")
    @Produces({MediaType.APPLICATION_JSON})
    public Map<String, Integer> getSedaInfo() {
        final SedaComponent seda = camelContext.getComponent("seda", SedaComponent.class);
        return seda.getQueues().entrySet().stream().collect(
                toMap(Entry::getKey, e -> e.getValue().getQueue().size())
        );
    }

    @GET
    @Path("total/inflights")
    @Produces({MediaType.TEXT_PLAIN})
    public Integer getInflightsCount() {
        return camelContext.getInflightRepository().browse().size();
    }
}
