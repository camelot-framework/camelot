package ru.yandex.qatools.camelot.core.builders;

import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class QuartzInitializerImpl implements QuartzInitializer {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final Scheduler scheduler;

    protected Lock lock;

    public QuartzInitializerImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    protected synchronized Lock getLock(){
        if (lock == null) {
            lock = new ReentrantLock();
        }
        return lock;
    }

    /**
     * Returns the Quartz lock within HazelCast
     */
    @Override
    public void lock() {
        getLock().lock();
    }

    /**
     * Starts the Quartz scheduler
     */
    @Override
    public synchronized void start() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (lockAndStartScheduler()) {
                        break;
                    }
                }
            }
        }).start();
    }

    private boolean lockAndStartScheduler() {
        try {
            lock();
            scheduler.start();
            logger.warn("This node is now the master Quartz!");
            return true;
        } catch (Exception e) {
            stop();
            logger.warn("Unable to start scheduler", e);
            return false;
        }
    }

    @Override
    public void standby() {
        try {
            scheduler.standby();
        } catch (Exception e) {
            logger.warn("Exception during the standby of the Quartz!", e);
        }
    }

    /**
     * Unlocks the Quartz scheduler
     */
    @Override
    public synchronized void stop() {
        try {
            scheduler.shutdown();
            unlock();
        } catch (IllegalStateException e) {
            logger.warn("Exception while trying to stop quartz lock: Locking service is already inactive!", e);
        } catch (Exception e) {
            logger.warn("Exception during the stop of the master Quartz!", e);
        }
    }

    @Override
    public void unlock() {
        getLock().unlock();
    }

}
