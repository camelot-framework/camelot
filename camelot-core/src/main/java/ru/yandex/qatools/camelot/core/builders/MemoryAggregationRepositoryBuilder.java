package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.impl.LocalMemoryStorage;
import ru.yandex.qatools.camelot.core.impl.MemoryAggregationRepository;

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
    public AggregationRepository initWritable(Plugin plugin) throws Exception {
        return new MemoryAggregationRepository(plugin, waitForLockSec);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Storage<T> initStorage(Plugin plugin) throws Exception {
        return new LocalMemoryStorage<>(new HashMap<String, T>());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> AggregatorRepository<T> initReadonly(final Plugin plugin) throws Exception {
        return new ReadonlyAggregatorRepository(camelContext, plugin);
    }
}
