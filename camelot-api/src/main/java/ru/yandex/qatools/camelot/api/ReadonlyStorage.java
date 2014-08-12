package ru.yandex.qatools.camelot.api;

import java.util.Set;

/**
 * Storage that allows only read operations
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ReadonlyStorage<T> {
    /**
     * Get the value of the key. Returns null if no value is present
     */
    T get(String key);

    /**
     * Get the list of the keys in the storage
     */
    Set<String> keys();

    /**
     * Get only the local keys (on the current node)
     */
    Set<String> localKeys();
}
