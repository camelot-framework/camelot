package ru.yandex.qatools.camelot.common.builders;

import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class QuartzMemoryInitializer extends AbstractQuartzInitializer<ReentrantLock> {

    public QuartzMemoryInitializer(Scheduler scheduler, AppConfig config) {
        super(scheduler, config);
    }

    @Override
    protected ReentrantLock initLock() {
        return new ReentrantLock();
    }
}
