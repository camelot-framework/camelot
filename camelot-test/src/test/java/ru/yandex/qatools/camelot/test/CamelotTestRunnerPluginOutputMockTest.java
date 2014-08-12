package ru.yandex.qatools.camelot.test;

import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@DisableTimers
public class CamelotTestRunnerPluginOutputMockTest {

    @EndpointPluginInput(TestAggregator.class)
    MockEndpoint aggInput;

    @EndpointPluginInput(TestProcessor.class)
    MockEndpoint prcInput;

    @EndpointPluginOutput(TestProcessor.class)
    MockEndpoint prcOutput;

    @EndpointPluginOutput(TestAggregator.class)
    MockEndpoint aggOutput;

    @Helper
    TestHelper helper;

    @Test
    public void testWithSteps() throws Exception {
        prcInput.expectedMessageCount(1);
        aggInput.expectedMessageCount(1);
        aggOutput.expectedMessageCount(1);
        prcOutput.expectedMessageCount(1);
        helper.send(10.0f, UUID, "uuid");
        prcInput.assertIsSatisfied();
        aggInput.assertIsSatisfied();
        prcOutput.assertIsSatisfied(5000);
        aggOutput.assertIsSatisfied(5000);
    }

    @Test
    public void testRegexp() throws Exception {
        verifyCleanEndpoint("activemq:plugin.some-plugin-id", "activemq:plugin.some-plugin-id?currentTimestamp=4534&asdasd=e3453");
        verifyCleanEndpoint("activemq:plugin.some-plugin-id", "activemq:plugin.some-plugin-id");
    }

    private void verifyCleanEndpoint(String cleanUri, String uri) {
        assertEquals(format("mock:%s", uri).replaceAll("^([^?]+)(\\?.+)?$", "$1"), "mock:" + cleanUri);
    }

}
