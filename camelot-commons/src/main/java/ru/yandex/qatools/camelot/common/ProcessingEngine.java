package ru.yandex.qatools.camelot.common;

import org.quartz.Scheduler;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ProcessingEngine extends PluginsService, RoutingService {

    /**
     * Set the scheduler for the engine
     */
    void setScheduler(Scheduler scheduler);

    /**
     * Returns the scehduler for the engine
     */
    Scheduler getScheduler();

    /**
     * Set the plugin initializer instance
     */
    void setPluginInitializer(PluginInitializer pluginInitializer);

    /**
     * Returns the plugin initializer
     */
    PluginInitializer getPluginInitializer();

}
