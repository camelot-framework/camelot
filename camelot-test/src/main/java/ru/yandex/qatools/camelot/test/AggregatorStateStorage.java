package ru.yandex.qatools.camelot.test;

import java.util.Set;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AggregatorStateStorage {
    <T> T getActual(String key);

    <T> T get(Class<T> stateClass, String key);

    Set<String> keys();
}
