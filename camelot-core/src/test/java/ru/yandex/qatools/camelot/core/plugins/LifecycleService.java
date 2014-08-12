package ru.yandex.qatools.camelot.core.plugins;

import org.springframework.context.annotation.Scope;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.PluginStorage;
import ru.yandex.qatools.camelot.api.annotations.Repository;

import javax.ws.rs.Path;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Scope("request")
@Path("/lifecycle")
public class LifecycleService {

    @Input
    EventProducer producer;

    @Repository
    private AggregatorRepository repository;

    @PluginStorage
    private Storage storage;

    public EventProducer getProducer() {
        return producer;
    }

    public AggregatorRepository getRepository() {
        return repository;
    }
}
