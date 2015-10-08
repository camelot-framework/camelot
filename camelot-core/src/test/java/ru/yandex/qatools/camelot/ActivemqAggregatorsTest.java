package ru.yandex.qatools.camelot;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ActivemqAggregatorsTest extends BasicPluginsTest {

    @EndpointInject(uri = "mock:direct:plugin.ru.yandex.qatools.camelot.core.plugins.WithoutIdAggregator.output")
    protected MockEndpoint endpointWithoutIdOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.all-skipped.input")
    protected MockEndpoint endpointAllSkippedInput;

    @EndpointInject(uri = "mock:direct:plugin.initializable.output")
    protected MockEndpoint endpointInitializableOutput;

    @EndpointInject(uri = "mock:direct:plugin.by-custom-strategy.output")
    protected MockEndpoint endpointByCustomStrategyOutput;

    @EndpointInject(uri = "mock:direct:plugin.filtered.output")
    protected MockEndpoint endpointFilteredOutput;

    @EndpointInject(uri = "mock:direct:plugin.custom-filtered.output")
    protected MockEndpoint endpointCustomFilteredOutput;

    @EndpointInject(uri = "mock:direct:plugin.lifecycle.output")
    protected MockEndpoint endpointLifecycleOutput;

    @EndpointInject(uri = "mock:direct:plugin.broken-by-label.output")
    protected MockEndpoint endpointBrokenByLabelOutput;

    @EndpointInject(uri = "mock:direct:plugin.all-skipped.output")
    protected MockEndpoint endpointAllSkippedOutput;

    @EndpointInject(uri = "mock:direct:plugin.by-hour-of-day.output")
    protected MockEndpoint endpointByHourOfDayOutput;

    @EndpointInject(uri = "mock:direct:plugin.test-started.output")
    protected MockEndpoint endpointTestStartedOutput;

    @EndpointInject(uri = "mock:direct:plugin.by-method.output")
    protected MockEndpoint endpointByMethodOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.fallen-raised.input")
    protected MockEndpoint endpointFallenRaisedInput;

    @EndpointInject(uri = "mock:direct:plugin.fallen-raised.output")
    protected MockEndpoint endpointFallenRaisedOutput;

    @EndpointInject(uri = "mock:direct:plugin.test-event-to-string.output")
    protected MockEndpoint endpointEventToStringOutput;

    @EndpointInject(uri = "mock:direct:plugin.by-custom-header.output")
    protected MockEndpoint endpointByCustomHeaderOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.by-custom-header.input")
    protected MockEndpoint endpointByCustomHeaderInput;

    @EndpointInject(uri = "mock:direct:plugin.with-timer.output")
    protected MockEndpoint endpointWithTimerOutput;

    @EndpointInject(uri = "mock:direct:plugin.dependent.output")
    protected MockEndpoint endpointDependentOutput;

    @EndpointInject(uri = "mock:direct:plugin.send-to-output.output")
    protected MockEndpoint endpointSendToOutput;

    @EndpointInject(uri = "mock:direct:plugin.bind-to-output.output")
    protected MockEndpoint endpointBindToOutput;

    @Produce(uri = "activemq:queue:events.input")
    protected ProducerTemplate inputQueue;
}
