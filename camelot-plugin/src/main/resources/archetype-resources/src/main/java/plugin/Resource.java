package ${groupId}.plugin;

import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.MainInput;
import ru.yandex.qatools.camelot.api.annotations.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

@Path("/plugin/")
public class Resource {

    @Repository
    AggregatorRepository repo;

    @MainInput
    EventProducer input;

    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Set<Object> getAggregated() {
        Set<Object> res = new HashSet<Object>();
        for(Object key : repo.keys()) {
            res.add(repo.get((String) key));
        }
        return res;
    }


    @PUT
    @Path("/events")
    @Consumes({MediaType.APPLICATION_XML})
    public Response sendMessage(String event) {
        input.produce(event);
        return Response.ok("ok").build();
    }
}
