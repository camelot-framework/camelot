package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import ru.yandex.qatools.camelot.api.Storage;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class HazelcastStorage<T> implements Storage<T> {
    private final Logger logger = getLogger(getClass());
    protected final IMap<String, T> map;

    public HazelcastStorage(HazelcastInstance hazelcastInstance, String repo) {
        this.map = hazelcastInstance.getMap(repo);
    }

    @Override
    public T get(String key) {
        return map.get(key);
    }

    @Override
    public Set<String> keys() {
        return map.keySet();
    }

    @Override
    public Set<String> localKeys() {
        return map.localKeySet();
    }

    @Override
    public void put(String key, T value) {
        map.put(key, value);
    }

    @Override
    public boolean lock(String key, long timeout, TimeUnit ofUnit) {
        try {
            return map.tryLock(key, timeout, ofUnit);
        } catch (Exception e) {
            logger.warn(String.format("Failed to lock storage by key %s", key), e);
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        try {
            map.unlock(key);
        } catch (Exception e) {
            logger.warn(String.format("Failed to lock storage by key %s", key), e);
        }
    }
}
