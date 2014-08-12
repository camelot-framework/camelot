package ru.yandex.qatools.camelot.core.web.jackson;

import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@javax.ws.rs.ext.Provider
@javax.ws.rs.Consumes({"application/json", "text/json"})
@javax.ws.rs.Produces({"application/json", "text/json"})
@Component
public class JacksonJaxbJsonProvider extends org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider implements JsonSerializer {

    @Override
    public String toJson(Object instance) throws IOException {
        return locateMapper(instance.getClass(), MediaType.APPLICATION_JSON_TYPE).writeValueAsString(instance);
    }
}
