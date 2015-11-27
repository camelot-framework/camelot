package ru.yandex.qatools.camelot.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializer;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializerFactory;

/**
 * @author Ilya Sadykov
 */
public class HazelcastQuartzInitializerFactory implements QuartzInitializerFactory {
    private final HazelcastInstance hazelcastInstance;

    public HazelcastQuartzInitializerFactory(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public QuartzInitializer newQuartzInitilizer(Scheduler scheduler, AppConfig config) {
        return new HazelcastQuartzInitializer(hazelcastInstance, scheduler, config);
    }
}
