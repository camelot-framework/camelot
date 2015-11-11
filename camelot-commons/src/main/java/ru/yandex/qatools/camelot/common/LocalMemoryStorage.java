package ru.yandex.qatools.camelot.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.Storage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalMemoryStorage<T> implements Storage<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalMemoryStorage.class);

    private Map<String, T> storage = new HashMap<>();
    private Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public LocalMemoryStorage() {
    }

    public LocalMemoryStorage(Map<String, T> storage) {
        this.storage = storage;
    }

    @Override
    public void put(String key, T value) {
        storage.put(key, value);
    }

    @Override
    public T get(String key) {
        return storage.get(key);
    }

    @Override
    public Set<String> keys() {
        return storage.keySet();
    }

    @Override
    public Map<String, T> valuesMap() {
        return storage;
    }

    @Override
    public boolean lock(String key, long timeout, TimeUnit ofUnit) {
        try {
            return getLock(key).tryLock(timeout, ofUnit);
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to lock storage by key %s", key), e);
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        try {
            getLock(key).unlock();
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to unlock storage by key %s", key), e);
        }
    }

    private synchronized ReentrantLock getLock(String key) {
        if (!locks.containsKey(key)) {
            locks.put(key, new ReentrantLock());
        }
        return locks.get(key);
    }
}
