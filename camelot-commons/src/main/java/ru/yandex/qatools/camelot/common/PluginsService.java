package ru.yandex.qatools.camelot.common;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.ClientSendersProvider;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginsInterop;
import ru.yandex.qatools.camelot.common.builders.BuildersFactory;
import ru.yandex.qatools.camelot.common.builders.QuartzInitializerFactory;
import ru.yandex.qatools.camelot.common.builders.ResourceBuilder;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.config.PluginTree;

import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface PluginsService {
    /**
     * Returns the plugin context by class
     */
    PluginContext getPluginContext(Class pluginClass);

    /**
     * Returns the plugin context by id
     */
    PluginContext getPluginContext(String pluginId);

    /**
     * Returns the configured plugin by id
     */
    Plugin getPlugin(String pluginId);

    /**
     * Returns the plugin by the class
     */
    Plugin getPlugin(Class pluginClass);

    /**
     * Returns the built plugins tree
     */
    PluginTree getPluginTree();

    /**
     * Returns the map of the loaded plugins. Key - pluginId.
     */
    Map<String, Plugin> getPluginsMap();

    /**
     * Returns the plugins map by class
     */
    Map<String, Plugin> getPluginsByClassMap();

    /**
     * Returns the loader for the plugin
     */
    PluginLoader getLoader();

    /**
     * Returns the plugins interoperability
     */
    PluginsInterop getInterop();

    /**
     * Returns the builders factory
     */
    BuildersFactory getBuildersFactory();

    /**
     * Set the builders factory
     */
    void setBuildersFactory(BuildersFactory buildersFactory);

    /**
     * Returns the plugin context injector
     */
    PluginContextInjector getContextInjector();

    /**
     * Set the plugin context injector
     */
    void setContextInjector(PluginContextInjector contextInjector);

    PluginUriBuilder getUriBuilder();

    void setUriBuilder(PluginUriBuilder uriBuilder);

    /**
     * Returns the resource builder
     */
    ResourceBuilder getResourceBuilder();

    /**
     * Set the resource builder for the plugin
     */
    void setResourceBuilder(ResourceBuilder resourceBuilder);

    /**
     * Get application config
     */
    AppConfig getAppConfig();

    /**
     * Set application config
     */
    void setAppConfig(AppConfig appConfig);

    /**
     * Get the CamelContext associated with this service
     */
    CamelContext getCamelContext();

    /**
     * Returns the engineName
     */
    String getEngineName();

    /**
     * Allows to redefine the engine name.
     * It is useful when there are several engines within the same Camel context
     */
    void setEngineName(String engineName);

    /**
     * Returns the main event producer
     */
    EventProducer getMainInput();

    /**
     * Returns messages serialized
     */
    MessagesSerializer getMessagesSerializer();

    /**
     * Set the default messages serializer
     */
    void setMessagesSerializer(MessagesSerializer messagesSerializer);

    /**
     * Returns quartz initialization factory
     */
    QuartzInitializerFactory getQuartzFactory();

    /**
     * Set the quartz initializer
     */
    void setQuartzFactory(QuartzInitializerFactory quartzInitializer);

    /**
     * Returns the current configured provider for client senders retrieval
     */
    ClientSendersProvider getClientSendersProvider();

    /**
     * Setups the client senders provider
     */
    void setClientSendersProvider(ClientSendersProvider clientSendersProvider);

    /**
     * Returns true if plugin has aggregator or processor
     */
    boolean pluginCanConsume(Plugin plugin);

    /**
     * Returns true when all plugins are initialized
     */
    boolean isInitialized();
}
