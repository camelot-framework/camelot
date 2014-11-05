package ru.yandex.qatools.camelot.core.plugins;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.EndpointListener;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.Listener;
import ru.yandex.qatools.camelot.api.annotations.PluginStorage;
import ru.yandex.qatools.camelot.api.annotations.Repository;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.builders.ReadonlyAggregatorRepository;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
@Scope("request")
@Path("/allSkipped")
public class AllSkippedService {

    @Input
    EventProducer producer;

    @Repository
    private AggregatorRepository repository;

    @Repository(ByLabelBrokenAggregator.class)
    private ReadonlyAggregatorRepository<CounterState> counterRepo;

    @PluginStorage
    private Storage storage;

    @Listener
    private EndpointListener listener;

    public EventProducer getProducer() {
        return producer;
    }

    public AggregatorRepository getRepository() {
        return repository;
    }

    public EndpointListener getListener() {
        return listener;
    }

    public ReadonlyAggregatorRepository<CounterState> getCounterRepo() {
        return counterRepo;
    }

    @GET
    @Path("/wait")
    public boolean waitAllSkipped() throws InterruptedException {
        final AtomicBoolean processed = new AtomicBoolean(false);
        listener.listen(20, SECONDS, new EndpointListener.Processor<Object>() {
            @Override
            public boolean onMessage(Object message, Map<String, Object> headers) {
                processed.set(message instanceof CounterState);
                return true;
            }
        });
        return processed.get();
    }
}
