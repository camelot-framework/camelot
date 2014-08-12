package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregationRepositoryBuilder {

    /**
     * Initialize the repository in r/w mode
     */
    AggregationRepository initWritable(Plugin plugin) throws Exception;


    /**
     * Initialize the storage for plugin
     */
    <T> Storage<T> initStorage(Plugin plugin) throws Exception;

    /**
     * Initialize the repository in readonly mode
     */
    <T> AggregatorRepository<T> initReadonly(Plugin plugin) throws Exception;
}
