package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.quartz.Scheduler;
import ru.qatools.mongodb.MongoPessimisticLock;
import ru.qatools.mongodb.MongoPessimisticLocking;
import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.AbstractQuartzInitializer;

import static java.lang.System.currentTimeMillis;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class MongodbQuartzInitializer extends AbstractQuartzInitializer<MongoPessimisticLock> {

    public static final String DEFAULT_QUARTZ_LOCK = "defaultQuartzLock";

    public static final String HEARTBEAT_LAST_TIME = "defaultQuartzHeartBeatTime";
    public static final String INITIALIZER_KS = "_quartz_";
    public static final int MAX_CHECK_INTERVAL_MS = 20000;

    private final MongoPessimisticRepo repo;

    public MongodbQuartzInitializer(MongoClient mongoClient, String dbName, Scheduler scheduler, AppConfig config) {
        super(scheduler, config);
        this.repo = new MongoPessimisticRepo(
                new MongoPessimisticLocking(
                        mongoClient, dbName, INITIALIZER_KS, MAX_CHECK_INTERVAL_MS
                )
        );
    }

    @Override
    protected MongoPessimisticLock initLock() {
        return new MongoPessimisticLock(repo.getLock(), DEFAULT_QUARTZ_LOCK);
    }

    @Override
    public void unlock() {
        getLock().forceUnlock();
    }

    @Override
    public long getLastHeartbeat() {
        final Object val = repo.get(HEARTBEAT_LAST_TIME);
        return (val != null) ? (long) val : 0L;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateHeartBeat() {
        repo.put(HEARTBEAT_LAST_TIME, currentTimeMillis());
    }

    @Override
    public boolean isMaster() {
        return super.getLock().isLockedByMe();
    }
}
