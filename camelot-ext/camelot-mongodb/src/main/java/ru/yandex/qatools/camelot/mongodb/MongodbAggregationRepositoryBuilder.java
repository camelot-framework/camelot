package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.spi.AggregationRepository;
import ru.qatools.mongodb.MongoPessimisticLocking;
import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.common.builders.MemoryAggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.config.Plugin;

import static ru.yandex.qatools.camelot.util.NameUtil.pluginStorageKey;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MongodbAggregationRepositoryBuilder extends MemoryAggregationRepositoryBuilder {

    private final MongoClient mongoClient;
    private final String dbName;
    private final long waitForLockSec;
    private final long lockPollMaxIntervalMs;
    private final MongoSerializer serializer;

    public MongodbAggregationRepositoryBuilder(MongoClient mongoClient, MessagesSerializer serializer, String dbName,
                                               CamelContext camelContext, long waitForLockSec, long lockPollMaxIntervalMs) {
        super(camelContext, waitForLockSec);
        this.mongoClient = mongoClient;
        this.waitForLockSec = waitForLockSec;
        this.lockPollMaxIntervalMs = lockPollMaxIntervalMs;
        this.dbName = dbName;
        this.serializer = new MongoSerializer(serializer);
    }

    /**
     * Initialize the HazelcastRepository instance
     */
    @Override
    public AggregationRepository initWritable(Plugin plugin) throws Exception { //NOSONAR
        final MongoPessimisticLocking locking = initLocking(plugin.getId());
        final MongodbAggregationRepository repo = new MongodbAggregationRepository(
                serializer, plugin.getId(), locking, waitForLockSec
        );
        repo.doStart();
        return repo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Storage initStorage(Plugin plugin) throws Exception { //NOSONAR
        return new MongodbStorage<>(new MongoPessimisticRepo<>(initLocking(pluginStorageKey(plugin.getId()))));
    }

    private MongoPessimisticLocking initLocking(String id) {
        return new MongoPessimisticLocking(mongoClient, dbName, id, lockPollMaxIntervalMs);
    }
}
