package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.core.impl.TestStateMachine.*;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-core-context.xml", "classpath:test-camelot-core-context.xml"})
@DirtiesContext
@MockEndpoints("*")
public class PluggableStateMachineStrategyTest {

    @EndpointInject(uri = "mock:seda:queue:done")
    protected MockEndpoint endpoint;

    @Produce(uri = "seda:queue:test")
    protected ProducerTemplate execute;

    @Rule
    public TestWatcher rule = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            endpoint.reset();
        }
    };

    @Test
    public void testFSM() throws Exception {
        endpoint.expectedMessageCount(1);
        endpoint.expectedBodyReceived().body().isInstanceOf(FinishedState.class);
        execute.sendBodyAndHeader(new TStartProgress("test"), BODY_CLASS, TStartProgress.class.getName());
        execute.sendBodyAndHeader(new TFinishProgress("test"), BODY_CLASS, TFinishProgress.class.getName());

        endpoint.assertIsSatisfied();
    }
}
