package ru.yandex.qatools.camelot.mongodb;

import org.slf4j.Logger;
import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.yandex.qatools.camelot.api.Storage;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class MongodbStorage<T extends Serializable> implements Storage<T> {
    private static final Logger LOGGER = getLogger(MongodbStorage.class);
    private final MongoPessimisticRepo<T> mongoRepo;

    public MongodbStorage(MongoPessimisticRepo<T> mongoRepo) {
        this.mongoRepo = mongoRepo;
    }

    @Override
    public T get(String key) {
        return mongoRepo.get(key);
    }

    @Override
    public Set<String> keys() {
        return mongoRepo.keySet();
    }

    @Override
    public void put(String key, T value) {
        mongoRepo.put(key, value);
    }

    @Override
    public boolean lock(String key, long timeout, TimeUnit ofUnit) {
        try {
            mongoRepo.getLock().tryLock(key, ofUnit.toMillis(timeout));
            return true;
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to lock storage by key %s", key), e);
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        try {
            mongoRepo.getLock().unlock(key);
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to lock storage by key %s", key), e);
        }
    }
}
