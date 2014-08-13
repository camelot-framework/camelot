package ru.yandex.qatools.camelot;

import com.hazelcast.core.HazelcastInstance;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.camelot.util.MapUtil;

import java.util.UUID;

import static org.junit.Assert.fail;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class BasicAggregatorsTest implements CamelContextAware {

    final protected Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    HazelcastInstance hazelcastInstance;

    @Autowired
    ProcessingEngine processingEngine;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.ru.yandex.qatools.camelot.core.plugins.WithoutIdAggregator")
    protected MockEndpoint endpointWithoutIdOutput;

    @EndpointInject(uri = "mock:ref:events.output")
    protected MockEndpoint endpointStop;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.initializable")
    protected MockEndpoint endpointInitializableOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.by-custom-strategy")
    protected MockEndpoint endpointByCustomStrategyOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.filtered")
    protected MockEndpoint endpointFilteredOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.custom-filtered")
    protected MockEndpoint endpointCustomFilteredOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.lifecycle")
    protected MockEndpoint endpointLifecycleOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.broken-by-label")
    protected MockEndpoint endpointBrokenByLabelOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.all-skipped")
    protected MockEndpoint endpointAllSkippedOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.by-hour-of-day")
    protected MockEndpoint endpointByHourOfDayOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.test-started")
    protected MockEndpoint endpointTestStartedOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.by-method")
    protected MockEndpoint endpointByMethodOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.input.fallen-raised")
    protected MockEndpoint endpointFallenRaisedInput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.fallen-raised")
    protected MockEndpoint endpointFallenRaisedOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.test-event-to-string")
    protected MockEndpoint endpointEventToStringOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.by-custom-header")
    protected MockEndpoint endpointByCustomHeader;

    @EndpointInject(uri = "mock:activemq:queue:plugin.input.by-custom-header")
    protected MockEndpoint endpointByCustomHeaderInput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.with-timer")
    protected MockEndpoint endpointWithTimer;

    @EndpointInject(uri = "mock:activemq:queue:plugin.output.dependent")
    protected MockEndpoint endpointDependent;

    @Produce(uri = "activemq:queue:events.input")
    protected ProducerTemplate inputQueue;

    protected final ClassLoader classLoader = getClass().getClassLoader();
    protected CamelContext camelContext;


    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
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
        getProducerTemplate(pluginId).sendBodyAndHeaders(testEvent, MapUtil.<String, Object>map(
                header, value,
                BODY_CLASS, testEvent.getClass().getName()
        ));
    }

    private ProducerTemplate getProducerTemplate(String pluginId) {
        ProducerTemplate template = camelContext.createProducerTemplate();
        template.setDefaultEndpointUri("activemq:queue:plugin.input." + pluginId);
        return template;
    }

    protected void sendTestEvent(String pluginId, Object testEvent) {
        getProducerTemplate(pluginId).sendBodyAndHeader(testEvent, BODY_CLASS, testEvent.getClass().getName());
    }

    protected void sendTestEvent(String pluginId, TestEvent testEvent, String uuid) {
        sendTestEvent(pluginId, testEvent, ru.yandex.qatools.camelot.api.Constants.Headers.UUID, uuid);
    }

    protected void sendEvent(Class pluginClass, Object event) {
        sendEvent(processingEngine.getPlugin(pluginClass).getId(), event);
    }

    protected void sendEvent(String pluginId, Object event) {
        getProducerTemplate(pluginId).sendBodyAndHeader(event, BODY_CLASS, event.getClass().getName());
    }
}
