package ru.yandex.qatools.camelot.core.builders;

import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class QuartzInitializerImpl implements QuartzInitializer {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected Lock lock;
    protected final Scheduler scheduler;

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
                try {
                    while (true) {
                        try {
                            lock();
                            logger.warn("This node is now the master Quartz!");
                            try {
                                scheduler.start();
                            } catch (Exception e) {
                                stop();
                                logger.warn("Some error occured during scheduler starting", e);
                                throw new PluginsSystemException(e);
                            }
                            return;
                        } catch (Exception e) {
                            scheduler.standby();
                            logger.warn("This node is not the master Quartz and failed to wait for the lock!", e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while trying to wait for the lock from Hazelcast!", e);
                }
            }
        }).start();
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
