package ru.yandex.qatools.camelot.core.builders;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface QuartzInitializer {
    /**
     * Start the scheduler
     */
    void start() throws Exception;

    /**
     * Pause the scheduler
     */
    void standby();

    /**
     * Stop the scheduler
     */
    void stop();

    /**
     * Get the lock for current schedulers cluster
     */
    void lock();

    /**
     * Unlock schedulers cluster
     */
    void unlock();

}
