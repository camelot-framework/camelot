package ru.yandex.qatools.camelot.common;

import java.util.Set;

/**
 * @author smecsia
 */
public interface AggregationRepositoryWithLocalKeys {
    /**
     * Returns all the keys that are stored locally
     */
    Set<String> localKeys();
}
