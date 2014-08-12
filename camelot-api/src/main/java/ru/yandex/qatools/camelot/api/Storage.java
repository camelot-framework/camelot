package ru.yandex.qatools.camelot.api;

/**
 * Storage, allowing the read/write operations
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface Storage<T> extends ReadonlyStorage<T>, LockableStorage {
    /**
     * Putting the value into the key
     */
    void put(String key, T value);
}
