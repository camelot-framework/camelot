package ru.yandex.qatools.camelot.core.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.common.builders.SchedulerBuilder;
import ru.yandex.qatools.camelot.common.builders.SchedulerBuildersFactory;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class NoSchedulerBuildersFactory implements SchedulerBuildersFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoSchedulerBuildersFactory.class);

    public NoSchedulerBuildersFactory() {
    }

    @Override
    public SchedulerBuilder build(final Plugin plugin) {
        return new SkippingSchedulerBuilder(plugin);
    }

    private static class SkippingSchedulerBuilder implements SchedulerBuilder {
        private final Plugin plugin;

        private SkippingSchedulerBuilder(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void schedule() throws Exception { //NOSONAR
            LOGGER.info("Skipping scheduling for the plugin {}", plugin.getId());
        }

        @Override
        public void unschedule() throws Exception { //NOSONAR
            LOGGER.info("Skipping unscheduling for the plugin {}", plugin.getId());
        }

        @Override
        public void invokeJobs() throws Exception { //NOSONAR
            LOGGER.info("Skipping invoking of the jobs for the plugin {}", plugin.getId());
        }

        @Override
        public boolean invokeJob(String method) throws Exception { //NOSONAR
            LOGGER.info("Skipping invoking of the job {} for the plugin {}", method, plugin.getId());
            return false;
        }
    }
}
