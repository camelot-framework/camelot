package ru.yandex.qatools.camelot;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.api.Constants;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.TestEvent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.fail;
import static ru.yandex.qatools.camelot.util.MapUtil.map;
import static ru.yandex.qatools.camelot.util.SerializeUtil.checkAndGetBytesInput;

/**
 * @author Ilya Sadykov
 */
public abstract class BasicPluginsTest implements CamelContextAware {

    final protected Logger logger = LoggerFactory.getLogger(getClass());
    protected final ClassLoader classLoader = getClass().getClassLoader();
    @EndpointInject(uri = "mock:ref:events.output")
    protected MockEndpoint endpointStop;
    protected CamelContext camelContext;
    @Autowired
    ProcessingEngine processingEngine;

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    protected void expectExchangeExists(MockEndpoint endpoint, String message, Predicate predicate) {
        for (Exchange exchange : endpoint.getExchanges()) {
            if (predicate.matches(exchange)) {
                return;
            }
        }
        fail(message);
    }


    protected String uuid() {
        return UUID.randomUUID().toString();
    }

    protected void sendStopEvent(String pluginId, StopEvent stopEvent) {
        sendTestEvent(pluginId, stopEvent, "stopEvent");
    }

    protected void sendTestEvent(String pluginId, TestEvent testEvent, String header, String value) {
        sendEvent(pluginId, testEvent, map(header, (Object) value));
    }

    protected void sendTestEvent(String pluginId, Object testEvent) {
        sendEvent(pluginId, testEvent);
    }

    protected void sendTestEvent(String pluginId, TestEvent testEvent, String uuid) {
        sendTestEvent(pluginId, testEvent, Constants.Headers.UUID, uuid);
    }

    protected void sendEvent(Class pluginClass, Object event) {
        sendEvent(processingEngine.getPlugin(pluginClass).getId(), event);
    }

    protected void sendEvent(String pluginId, Object event) {
        sendEvent(pluginId, event, new HashMap<String, Object>());
    }

    protected void sendEvent(String pluginId, Object event, Map<String, Object> headers) {
        processingEngine.getPlugin(pluginId).getContext().getInput().produce(event, headers);
    }

    protected  <T extends Serializable> T getInput(Exchange exchange, Class<T> expectedType) {
        return checkAndGetBytesInput(expectedType,
                exchange.getIn().getBody(), processingEngine.getMessagesSerializer(), classLoader);
    }
}
