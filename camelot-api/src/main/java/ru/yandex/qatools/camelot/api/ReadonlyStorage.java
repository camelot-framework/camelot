package ru.yandex.qatools.camelot.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Storage that allows only read operations
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
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
    default Set<String> localKeys() {
        return keys();
    }

    /**
     * Returns a map of all keys to the corresponding states
     */
    default Map<String, T> valuesMap() {
        Set<String> keys = keys();
        Map<String, T> result = new HashMap<>(keys.size(), 1);
        for (String key : keys) {
            result.put(key, get(key));
        }
        return result;
    }
}
