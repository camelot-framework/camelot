package ru.yandex.qatools.camelot.core.web.jackson;

import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Provider
@Consumes({"application/json", "text/json"})
@Produces({"application/json", "text/json"})
@Component
public class JacksonJaxbJsonProvider
        extends com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
        implements JsonSerializer {

    @Override
    public String toJson(Object instance) throws IOException {
        return locateMapper(instance.getClass(), MediaType.APPLICATION_JSON_TYPE)
                .writeValueAsString(instance);
    }
}
