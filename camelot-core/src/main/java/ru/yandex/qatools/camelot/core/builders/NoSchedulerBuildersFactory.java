package ru.yandex.qatools.camelot.core.builders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        public void schedule() throws Exception {
            LOGGER.info(String.format("Skipping scheduling for the plugin %s", plugin.getId()));
        }

        @Override
        public void unschedule() throws Exception {
            LOGGER.info(String.format("Skipping unscheduling for the plugin %s", plugin.getId()));
        }

        @Override
        public void invokeJobs() throws Exception {
            LOGGER.info(String.format("Skipping invoking of the jobs for the plugin %s", plugin.getId()));
        }

        @Override
        public boolean invokeJob(String method) throws Exception {
            LOGGER.info(String.format("Skipping invoking of the job %s for the plugin %s", method, plugin.getId()));
            return false;
        }
    }
}
