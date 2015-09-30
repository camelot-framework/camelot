package ru.yandex.qatools.camelot;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ActivemqAggregatorsTest extends BasicPluginsTest {

    @EndpointInject(uri = "mock:seda:plugin.ru.yandex.qatools.camelot.core.plugins.WithoutIdAggregator.output")
    protected MockEndpoint endpointWithoutIdOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.input.all-skipped")
    protected MockEndpoint endpointAllSkippedInput;

    @EndpointInject(uri = "mock:seda:plugin.initializable.output")
    protected MockEndpoint endpointInitializableOutput;

    @EndpointInject(uri = "mock:seda:plugin.by-custom-strategy.output")
    protected MockEndpoint endpointByCustomStrategyOutput;

    @EndpointInject(uri = "mock:seda:plugin.filtered.output")
    protected MockEndpoint endpointFilteredOutput;

    @EndpointInject(uri = "mock:seda:plugin.custom-filtered.output")
    protected MockEndpoint endpointCustomFilteredOutput;

    @EndpointInject(uri = "mock:seda:plugin.lifecycle.output")
    protected MockEndpoint endpointLifecycleOutput;

    @EndpointInject(uri = "mock:seda:plugin.broken-by-label.output")
    protected MockEndpoint endpointBrokenByLabelOutput;

    @EndpointInject(uri = "mock:seda:plugin.all-skipped.output")
    protected MockEndpoint endpointAllSkippedOutput;

    @EndpointInject(uri = "mock:seda:plugin.by-hour-of-day.output")
    protected MockEndpoint endpointByHourOfDayOutput;

    @EndpointInject(uri = "mock:seda:plugin.test-started.output")
    protected MockEndpoint endpointTestStartedOutput;

    @EndpointInject(uri = "mock:seda:plugin.by-method.output")
    protected MockEndpoint endpointByMethodOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.input.fallen-raised")
    protected MockEndpoint endpointFallenRaisedInput;

    @EndpointInject(uri = "mock:seda:plugin.fallen-raised.output")
    protected MockEndpoint endpointFallenRaisedOutput;

    @EndpointInject(uri = "mock:seda:plugin.test-event-to-string.output")
    protected MockEndpoint endpointEventToStringOutput;

    @EndpointInject(uri = "mock:seda:plugin.by-custom-header.output")
    protected MockEndpoint endpointByCustomHeaderOutput;

    @EndpointInject(uri = "mock:activemq:queue:plugin.input.by-custom-header")
    protected MockEndpoint endpointByCustomHeaderInput;

    @EndpointInject(uri = "mock:seda:plugin.with-timer.output")
    protected MockEndpoint endpointWithTimerOutput;

    @EndpointInject(uri = "mock:seda:plugin.dependent.output")
    protected MockEndpoint endpointDependentOutput;

    @EndpointInject(uri = "mock:seda:plugin.send-to-output.output")
    protected MockEndpoint endpointSendToOutput;

    @EndpointInject(uri = "mock:seda:plugin.bind-to-output.output")
    protected MockEndpoint endpointBindToOutput;

    @Produce(uri = "activemq:queue:events.input")
    protected ProducerTemplate inputQueue;
}
