package ru.yandex.qatools.camelot.web.core;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.common.PluginLoader;
import ru.yandex.qatools.camelot.config.*;
import ru.yandex.qatools.camelot.core.impl.GenericPluginsEngine;
import ru.yandex.qatools.camelot.web.Broadcaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.yandex.qatools.camelot.util.ContextUtils.autowireFields;
import static ru.yandex.qatools.camelot.util.NameUtil.routeId;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class WebfrontEngineImpl extends GenericPluginsEngine implements WebfrontEngine, ApplicationContextAware {

    private ApplicationContext applicationContext;

    public WebfrontEngineImpl(Resource[] configResources, PluginLoader pluginLoader,
                              CamelContext camelContext,
                              String inputUri, String outputUri) {
        super(configResources, pluginLoader, camelContext, inputUri, outputUri);
    }

    /**
     * Initialize the service
     */
    @Override
    public synchronized void init() {
        super.init();
        try {
            initWebResources();
            initAdditionalProcessors();
        } catch (Exception e) {
            logger.error("Could not intialize web context", e);
        }
        initialized = true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Creates/gets the broadcaster for the plugin
     */
    @Override
    public Broadcaster getBroadcaster(String pluginId, String topic) {
        try {
            return getPlugin(pluginId).getContext().getLocalBroadcastersProvider().getBroadcaster(topic);
        } catch (Exception e) {
            logger.error("Failed to return the broadcaster for the pluginId " + pluginId + " and topic " + topic, e);
            return null;
        }
    }

    @Override
    public PluginWeb getPlugin(String pluginId) {
        return (PluginWeb) super.getPlugin(pluginId);
    }

    @Override
    public PluginWebContext getPluginContext(String pluginId) {
        return (PluginWebContext) super.getPluginContext(pluginId);
    }

    @Override
    public void enableLocalBroadcasters() {
        for (PluginWeb plugin : getPlugins()) {
            if (plugin.getContext().getLocalBroadcastersProvider() != null) {
                plugin.getContext().getLocalBroadcastersProvider().enable();
            }
        }
    }

    @Override
    public void disableLocalBroadcasters() {
        for (PluginWeb plugin : getPlugins()) {
            if (plugin.getContext().getLocalBroadcastersProvider() != null) {
                plugin.getContext().getLocalBroadcastersProvider().disable();
            }
        }
    }


    @Override
    public Collection<PluginWeb> getPlugins() {
        List<PluginWeb> result = new ArrayList<>();
        for (Plugin plugin : getPluginsMap().values()) {
            result.add((PluginWeb) plugin);
        }
        return result;
    }


    /**
     * ------------------------------------------------------------- *
     */

    @Override
    protected void initPluginsContexts(PluginsConfig config) throws Exception { //NOSONAR
        for (final PluginsSource source : config.getSources()) {
            final ClassLoader classLoader = pluginLoader.createClassLoader(source);
            List<PluginWeb> plugins = new ArrayList<>();
            for (final Plugin plugin : source.getPlugins()) {
                final PluginWeb pluginWeb = new PluginWeb(plugin, new PluginWebContext());
                plugins.add(pluginWeb);
                pluginWeb.getContext().setLocalBroadcastersProvider(
                        new AtmosphereClientBroadcastersProvider(
                                applicationContext,
                                pluginWeb
                        )
                );
                initPluginContext(source, pluginWeb, pluginWeb.getContext(), classLoader);
            }
            source.getPlugins().clear();
            source.getPlugins().addAll(plugins);
        }
    }

    /**
     * Returns the client notify queue or topic uri
     */
    protected String frontendBroadcastUri() {
        return getUriBuilder().frontendBroadcastUri();
    }

    /**
     * ------------------------------------------------------------- *
     */

    /**
     * Initialize the additional processors
     */
    private void initAdditionalProcessors() throws Exception { //NOSONAR
        initLocalFrontendNotifier();
    }

    /**
     * Initialize the local notifier processor
     */
    private void initLocalFrontendNotifier() throws Exception { //NOSONAR
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                final LocalPluginFrontendNotifier processor = new LocalPluginFrontendNotifier(getMessagesSerializer());
                autowireFields(processor, applicationContext, camelContext);
                addInterimRoute(from(frontendBroadcastUri()).process(processor))
                        .stop()
                        .routeId(routeId(frontendBroadcastUri(), getEngineName()));
            }
        });
    }

}
