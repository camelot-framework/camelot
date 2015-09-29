package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import java.util.Set;

import static ru.yandex.qatools.camelot.util.NameUtil.pluginStorageKey;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class HazelcastAggregationRepositoryBuilder implements AggregationRepositoryBuilder {

    private final HazelcastInstance hazelcastInstance;
    private final long waitForLockSec;
    private final long lockWaitHeartBeatSec;

    public HazelcastAggregationRepositoryBuilder(HazelcastInstance hazelcastInstance, CamelContext camelContext,
                                                 long waitForLockSec, long lockWaitHeartBeatSec) {
        this.hazelcastInstance = hazelcastInstance;
        this.waitForLockSec = waitForLockSec;
        this.lockWaitHeartBeatSec = lockWaitHeartBeatSec;
    }

    /**
     * Initialize the HazelcastRepository instance
     */
    @Override
    public AggregationRepository initWritable(Plugin plugin) throws Exception { //NOSONAR
        final HazelcastAggregationRepository aggregationRepository = new LocalKeysHazelcastAggregatorRepository();
        aggregationRepository.setRepository(plugin.getId());
        aggregationRepository.setHazelcastInstance(hazelcastInstance);
        aggregationRepository.setWaitForLockSec(waitForLockSec);
        aggregationRepository.setLockWaitHeartbeatSec(lockWaitHeartBeatSec);
        aggregationRepository.doStart();
        return aggregationRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Storage<T> initStorage(Plugin plugin) throws Exception { //NOSONAR
        return new HazelcastStorage<>(hazelcastInstance, pluginStorageKey(plugin.getId()));
    }

    @Override
    public <T> AggregatorRepository<T> initReadonly(Plugin plugin) throws Exception { //NOSONAR
        final Storage<T> storage = initStorage(plugin);
        return new AggregatorRepository<T>() {
            @Override
            public T get(String key) {
                return storage.get(key);
            }

            @Override
            public Set<String> keys() {
                return storage.keys();
            }

            @Override
            public Set<String> localKeys() {
                return storage.localKeys();
            }
        };
    }

}
