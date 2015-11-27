package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializer;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializerFactory;

/**
 * @author Ilya Sadykov
 */
public class MongodbQuartzInitializerFactory implements QuartzInitializerFactory {
    private final MongoClient mongoClient;
    private final String dbName;

    public MongodbQuartzInitializerFactory(MongoClient mongoClient, String dbName) {
        this.mongoClient = mongoClient;
        this.dbName = dbName;
    }

    @Override
    public QuartzInitializer newQuartzInitilizer(Scheduler scheduler, AppConfig config) {
        return new MongodbQuartzInitializer(mongoClient, dbName, scheduler, config);
    }
}
