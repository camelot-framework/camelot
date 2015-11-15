package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.config.PluginContext;

/**
 * Helper class to inject all the required dependencies into the plugin instance
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginContextInjector<P> {
    /**
     * Inject the context into the plugin instance when exchange is not available
     */
    void inject(final P pluginObj, final PluginContext pluginContext);

    /**
     * Inject the context into the plugin instance when exchange is not available
     */
    void inject(final P pluginObj, final PluginsService service, final PluginContext pluginContext);

    /**
     * Inject the context into the plugin instance
     */
    void inject(final P pluginObj, final PluginContext pluginContext, final Exchange exchange);

    /**
     * Inject the context into the plugin instance
     */
    void inject(final P pluginObj, final PluginsService service,
                final PluginContext pluginContext, final Exchange exchange);

}
