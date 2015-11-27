package ru.yandex.qatools.camelot.common.builders;

import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;

/**
 * @author Ilya Sadykov
 */
public interface QuartzInitializerFactory {
    default QuartzInitializer newQuartzInitilizer(Scheduler scheduler, AppConfig config) {
        return new BasicQuartzInitializer(scheduler, config);
    }
}
