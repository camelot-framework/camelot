package ru.yandex.qatools.camelot.core.builders;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface QuartzInitializer {
    /**
     * Start the scheduler
     */
    void start();

    void restart();

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
    boolean lock() throws InterruptedException;

    /**
     * Unlock schedulers cluster
     */
    void unlock();


    /**
     * Updates heart beat of master node
     */
    void updateHeartBeat();

    boolean isMaster();

    /**
     * Returns last heart beat of master node
     */
    long getLastHeartbeat();
}
