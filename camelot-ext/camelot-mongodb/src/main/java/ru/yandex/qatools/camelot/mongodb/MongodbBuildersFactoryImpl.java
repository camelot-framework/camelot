package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.common.builders.AggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.common.builders.BuildersFactoryImpl;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MongodbBuildersFactoryImpl extends BuildersFactoryImpl {

    protected long lockPollMaxIntervalMs = 20;
    protected final MongoClient mongoClient;
    protected final String dbName;

    public MongodbBuildersFactoryImpl(MongoClient mongoClient, String dbName) {
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
}
