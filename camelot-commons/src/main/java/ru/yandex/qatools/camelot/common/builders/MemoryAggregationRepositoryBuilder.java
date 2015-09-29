package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.common.LocalMemoryStorage;
import ru.yandex.qatools.camelot.common.MemoryAggregationRepository;
import ru.yandex.qatools.camelot.config.Plugin;

import java.util.HashMap;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MemoryAggregationRepositoryBuilder implements AggregationRepositoryBuilder {

    private final CamelContext camelContext;
    private final long waitForLockSec;

    public MemoryAggregationRepositoryBuilder(CamelContext camelContext, long waitForLockSec) {
        this.camelContext = camelContext;
        this.waitForLockSec = waitForLockSec;
    }

    /**
     * Initialize the HazelcastRepository instance
     */
    @Override
    public AggregationRepository initWritable(Plugin plugin) throws Exception { //NOSONAR
        return new MemoryAggregationRepository(plugin, waitForLockSec);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Storage<T> initStorage(Plugin plugin) throws Exception { //NOSONAR
        return new LocalMemoryStorage<>(new HashMap<String, T>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> AggregatorRepository<T> initReadonly(final Plugin plugin) throws Exception { //NOSONAR
        return new ReadonlyAggregatorRepository(camelContext, plugin);
    }
}
