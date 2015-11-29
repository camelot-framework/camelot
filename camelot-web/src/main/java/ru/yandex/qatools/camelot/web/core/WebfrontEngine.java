package ru.yandex.qatools.camelot.web.core;

import org.atmosphere.cpr.Broadcaster;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.PluginWeb;
import ru.yandex.qatools.camelot.config.PluginWebContext;

import java.util.Collection;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface WebfrontEngine extends PluginsService {
    /**
     * Creates/gets the broadcaster for the plugin
     */
    Broadcaster getBroadcaster(String pluginId, String topic);

    /**
     * Returns web enabled plugin
     */
    PluginWeb getPlugin(String pluginId);

    /**
     * Returns the plugin config by id
     */
    PluginWebContext getPluginContext(String pluginId);

    /**
     * Returns collection of currently used plugins
     */
    Collection<PluginWeb> getPlugins();

    /**
     * Enable all local broadcasters
     */
    void enableLocalBroadcasters();

    /**
     * Disable all local broadcasters
     */
    void disableLocalBroadcasters();
}
