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
import ru.yandex.qatools.camelot.core.impl.IncomingMessagesQueueProcessor;
import ru.yandex.qatools.camelot.web.ApiResource;
import ru.yandex.qatools.camelot.web.Broadcaster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static ru.yandex.qatools.camelot.util.ContextUtils.autowireFields;
import static ru.yandex.qatools.camelot.util.NameUtil.routeId;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveEndpoints;
import static ru.yandex.qatools.camelot.util.ServiceUtil.gracefullyRemoveRoute;

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
        startLoading();

        try {
            initAdditionalProcessors();
        } catch (Exception e) {
            logger.error("Could not register local client notify routes", e);
        }
        stopLoading();
    }

    @Override
    public synchronized void reloadAndStart() {
        super.reloadAndStart();
        startLoading();
        try {
            disableLocalBroadcasters();
            if (applicationContext.getBean(SpringServletFacade.class) != null) {
                applicationContext.getBean(SpringServletFacade.class).restartQuietly();
            }
            if (applicationContext.getBean(ApiResource.class) != null) {
                applicationContext.getBean(ApiResource.class).emptyCache();
            }
            initAdditionalProcessors();
            enableLocalBroadcasters();
        } catch (Exception e) {
            logger.error("Failed to reload and start plugins!", e);
        }
        stopLoading();
    }

    @Override
    public synchronized void stop() {
        super.stop();
        try {
            stopAdditionalProcessors();
        } catch (Exception e) {
            logger.error("Failed to stop services!", e);
        }
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
     * Returns the temporary buffer queue uri
     */
    protected String tmpBufferUri() {
        return getUriBuilder().tmpInputBufferUri();
    }

    /**
     * Returns the client notify queue or topic uri
     */
    protected String frontendNotifyUri() {
        return getUriBuilder().frontendBroadcastUri();
    }

    /**
     * Initializing the plugin context
     */
    @Override
    protected void initPluginContext(PluginsSource source, Plugin plugin,
                                     PluginContext context, ClassLoader classLoader) throws Exception { //NOSONAR
        super.initPluginContext(source, plugin, context, classLoader);
        context.setTmpBufferUri(tmpBufferUri());
        context.setFrontendNotifyUri(frontendNotifyUri());
    }

    /**
     * ------------------------------------------------------------- *
     */

    /**
     * Stop the additional processors
     */
    private void stopAdditionalProcessors() throws Exception { //NOSONAR
        gracefullyRemoveRoute(camelContext, frontendNotifyUri());
        gracefullyRemoveEndpoints(camelContext, frontendNotifyUri());
        gracefullyRemoveRoute(camelContext, tmpBufferUri());
        gracefullyRemoveEndpoints(camelContext, tmpBufferUri());
    }

    /**
     * Initialize the additional processors
     */
    private void initAdditionalProcessors() throws Exception { //NOSONAR
        initLocalFrontendNotifier();
        initTmpInputQueueProcessor();
    }

    /**
     * Initialize the input queue processor
     */
    private void initTmpInputQueueProcessor() throws Exception { //NOSONAR
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception { //NOSONAR
                final IncomingMessagesQueueProcessor processor = new IncomingMessagesQueueProcessor(camelContext, getMessagesSerializer());
                autowireFields(processor, applicationContext, camelContext);
                addInterimRoute(from(tmpBufferUri()).process(processor)).stop().routeId(routeId(tmpBufferUri(), getEngineName()));
            }
        });
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
                addInterimRoute(from(frontendNotifyUri()).process(processor)).stop().routeId(routeId(frontendNotifyUri(), getEngineName()));
            }
        });
    }

}
