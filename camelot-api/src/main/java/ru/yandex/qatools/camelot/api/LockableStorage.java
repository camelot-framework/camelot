package ru.yandex.qatools.camelot.api;

import java.util.concurrent.TimeUnit;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface LockableStorage {

    /**
     * Tries to lock the key within storage. Blocks the execution for the given timeout.
     * If lock is not succeeded within the given timeout, it releases the blocking.
     */
    boolean lock(String key, long timeout, TimeUnit ofUnit);

    /**
     * Unlocks the key within storage. Method is not blocking and returns instantly.
     */
    void unlock(String key);
}
