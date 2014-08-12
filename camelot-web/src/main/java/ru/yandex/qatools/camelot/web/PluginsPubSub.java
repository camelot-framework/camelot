package ru.yandex.qatools.camelot.web;

import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.springframework.context.ApplicationContext;
import ru.yandex.qatools.camelot.core.WebfrontEngine;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Path("/plugins/{pluginId}")
public class PluginsPubSub {

    @Context
    ServletContext servletContext;

    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    public EventOutput subscribe(@PathParam("pluginId") String pluginId, @QueryParam("topic") String topic) {
        final EventOutput eventOutput = new EventOutput();
        getBroadcaster(pluginId, topic).add(eventOutput);
        return eventOutput;
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public String publish(@FormParam("message") String message, @PathParam("pluginId") String pluginId, @QueryParam("topic") String topic) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent event = eventBuilder.name("message")
                .mediaType(MediaType.TEXT_PLAIN_TYPE)
                .data(String.class, message)
                .build();

        getBroadcaster(pluginId, topic).broadcast(event);
        return "Message '" + message + "' has been broadcast to topic " + topic + ".";
    }


    private SseBroadcaster getBroadcaster(String pluginId, String topic) {
        ApplicationContext context = getWebApplicationContext(servletContext);
        if (context != null) {
            return context.getBean(WebfrontEngine.class).getBroadcaster(pluginId, topic);
        }
        return null;
    }


}
