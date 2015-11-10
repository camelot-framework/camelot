package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.common.builders.BuildersFactoryImpl;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BuildersFactoryWithMongodbImpl extends BuildersFactoryImpl {

    protected long lockPollMaxIntervalMs = 20;
    protected final MongoClient mongoClient;
    protected final String dbName;

    public BuildersFactoryWithMongodbImpl(MongoClient mongoClient, String dbName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
    }

    public void setLockPollMaxIntervalMs(long lockPollMaxIntervalMs) {
        this.lockPollMaxIntervalMs = lockPollMaxIntervalMs;
    }

    @Override
    public AggregationRepositoryBuilder newRepositoryBuilder(CamelContext camelContext) throws Exception { //NOSONAR
        return new MongodbAggregationRepositoryBuilder(mongoClient, dbName, camelContext,
                getWaitForLockSec(), lockPollMaxIntervalMs);
    }

    @Override
    public QuartzInitializer newQuartzInitializer(Scheduler scheduler, AppConfig config) throws Exception { //NOSONAR
        return new QuartzMongodbInitializerImpl(mongoClient, dbName, scheduler, config);
    }
}
