package ru.yandex.qatools.camelot.util;

import ru.yandex.qatools.camelot.config.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ilya Sadykov
 * @version $Date$ $Revision$
 */
public abstract class MapUtil {

    MapUtil() {
    }

    /**
     * Creates the HashMap of Integers
     *
     * @param key        first key of the map
     * @param value      first value of the map
     * @param keysValues pairs of key value
     *                   Example: sMap(1, 2, 2, 4) will create {1 -> 2, 2 -> 4}
     * @return new HashMap of String -> String
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> map(K key, V value, Object... keysValues) {
        Map<K, V> res = new HashMap<>();
        res.put(key, value);
        if (keysValues.length > 0) {
            if (keysValues.length % 2 > 0) {
                throw new IllegalArgumentException("Arguments count must be even!");
            }
            for (int i = 0; i < keysValues.length; i += 2) {
                K k = (K) keysValues[i];
                V v = (V) keysValues[i + 1];
                res.put(k, v);
            }
        }
        return res;
    }

    /**
     * Convert list of given {@link ru.yandex.qatools.camelot.config.Parameter}
     *
     * @param params given parameters to convert
     */
    public static Map<String, String> mapParams(List<Parameter> params) {
        Map<String, String> res = new HashMap<>();
        for (Parameter parameter : params) {
            res.put(parameter.getName(), parameter.getValue());
        }
        return res;
    }
}
