package ru.yandex.qatools.camelot.core.plugins;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.MainInput;
import ru.yandex.qatools.camelot.api.annotations.PluginStorage;
import ru.yandex.qatools.camelot.api.annotations.Repository;
import ru.yandex.qatools.camelot.common.builders.ReadonlyAggregatorRepository;
import ru.yandex.qatools.camelot.core.beans.CounterState;

import javax.ws.rs.Path;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
@Scope("request")
@Path("/allSkipped")
public class AllSkippedService {

    @Input
    EventProducer producer;

    @MainInput
    EventProducer mainProducer;

    @Repository
    private AggregatorRepository repository;

    @Repository(ByLabelBrokenAggregator.class)
    private ReadonlyAggregatorRepository<CounterState> counterRepo;

    @PluginStorage
    private Storage storage;

    public EventProducer getProducer() {
        return producer;
    }

    public EventProducer getMainProducer() {
        return mainProducer;
    }

    public AggregatorRepository getRepository() {
        return repository;
    }

    public ReadonlyAggregatorRepository<CounterState> getCounterRepo() {
        return counterRepo;
    }
}
