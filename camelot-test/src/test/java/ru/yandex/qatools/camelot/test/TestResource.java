package ru.yandex.qatools.camelot.test;

import ru.yandex.qatools.camelot.api.EndpointListener;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Path("/test")
public class TestResource {

    @Input
    EventProducer input;

    @Listener
    EndpointListener endpoint;

    @ConfigValue("camelot-test.someTestingProperty")
    String property = "";

    @GET
    @Path("/test-res")
    public boolean checkRoutes() {
        final String uuid = randomUUID().toString();
        final AtomicBoolean checkSuccess = new AtomicBoolean(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                input.produce(uuid);
            }
        }.start();
        try {
            endpoint.listen(30, SECONDS, new EndpointListener.Processor<Object>() {
                @Override
                public boolean onMessage(Object message, Map<String, Object> headers) {
                    if (message instanceof String) {
                        checkSuccess.set(message.equals(uuid + "-processed-" + property));
                        return true;
                    }
                    return false;
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return checkSuccess.get();
    }
}
