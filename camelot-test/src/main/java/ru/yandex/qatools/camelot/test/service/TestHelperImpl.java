package ru.yandex.qatools.camelot.test.service;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.test.TestHelper;

import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestHelperImpl implements CamelContextAware, TestHelper {
    final private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    ProcessingEngine pluginsService;

    @Autowired
    HazelcastInstance hazelcastInstance;

    ProducerTemplate producerTemplate;
    CamelContext camelContext;

    @Override
    public void sendTo(Class pluginClass, Object event) {
        pluginsService.getPluginContext(pluginClass).getInput().produce(event);
    }

    @Override
    public void sendTo(String pluginId, Object event) {
        pluginsService.getPluginContext(pluginId).getInput().produce(event);
    }

    @Override
    public void sendTo(Class pluginClass, Object event, Map<String, Object> headers) {
        pluginsService.getPluginContext(pluginClass).getInput().produce(event, headers);
    }

    @Override
    public void sendTo(Class pluginClass, Object event, String header, Object headerValue) {
        sendTo(pluginClass, event, map(header, headerValue));
    }

    @Override
    public void sendTo(String pluginId, Object event, Map<String, Object> headers) {
        pluginsService.getPluginContext(pluginId).getInput().produce(event, headers);
    }

    @Override
    public void sendTo(String pluginId, Object event, String header, Object headerValue) {
        sendTo(pluginId, event, map(header, headerValue));
    }

    @Override
    public void send(Object event) {
        producerTemplate.sendBodyAndHeader(event, BODY_CLASS, event.getClass().getName());
    }

    @Override
    public void send(Object event, Map<String, Object> headers) {
        headers.put(BODY_CLASS, event.getClass().getName());
        producerTemplate.sendBodyAndHeaders(event, headers);
    }

    @Override
    public void send(Object event, String header, Object headerValue) {
        send(event, map(header, headerValue));
    }

    @Override
    public void invokeTimersFor(Class pluginClass) {
        invokeTimers(pluginsService.getPlugin(pluginClass));
    }

    @Override
    public void invokeTimersFor(String pluginId) {
        invokeTimers(pluginsService.getPlugin(pluginId));
    }

    @Override
    public void invokeTimers(Plugin plugin){
        try {
            pluginsService.getBuildersFactory().newSchedulerBuildersFactory(
                    pluginsService.getScheduler(),
                    pluginsService.getCamelContext()
            ).build(plugin).invokeJobs();
        } catch (Exception e) {
            logger.error("Failed to invoke timers for the plugin %s", plugin.getId(), e);
        }
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.setDefaultEndpointUri(pluginsService.getInputUri());
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
