package ru.yandex.qatools.camelot.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.matchers.decorators.MatcherDecorators;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseProperties("camelot-test.properties")
@DisableTimers
@CamelotTestConfig(components = {
        @OverrideComponent(from = SomeComponent.class, to = AnotherComponent.class)
})
public class CamelotTestRunnerTest {

    private static final int TIMEOUT = 3000;
    public static final String CHECK_VALUE = "test1processedtestValue";

    @PluginMock
    TestAggregator aggMock;

    @PluginMock
    TestProcessor prcMock;

    @Helper
    TestHelper helper;

    @AggregatorState(TestAggregator.class)
    AggregatorStateStorage stateStorage;

    @ClientSenderMock(TestAggregator.class)
    ClientMessageSender sender;

    @ClientSenderMock(value = TestAggregator.class, topic = "test")
    ClientMessageSender senderTopic;

    @Resource(TestProcessor.class)
    TestResource testResource;

    @Test
    public void testRoute() throws Exception {
        helper.send("test1", UUID, "uuid1");
        helper.send("test1", UUID, "uuid2");

        verify(prcMock, timeout(TIMEOUT).times(2)).onNodeEvent(eq("test1"));
        verify(aggMock, timeout(TIMEOUT).times(2)).onNodeEvent(any(TestState.class), eq(CHECK_VALUE));
        verify(sender, timeout(TIMEOUT).times(2)).send(any(TestState.class));
        verify(senderTopic, timeout(TIMEOUT).times(2)).send(eq(AnotherComponent.class.getName()));

        verify(aggMock, timeout(5000).never()).resetState(any(TestState.class));

        TestState state = stateStorage.get(TestState.class, "uuid1");
        assertThat(state, should(having(
                on(TestState.class).getMessage(), equalTo(CHECK_VALUE))
        ).whileWaitingUntil(MatcherDecorators.timeoutHasExpired(TIMEOUT)));
    }

    @Test
    public void testRouteWithinResource() throws Exception {
        assertTrue("Failed to check routes: must return true", testResource.checkRoutes());
    }

    @Test
    public void testRouteAgain() throws Exception {
        helper.send("test2", UUID, "uuid3");
        verify(prcMock, timeout(TIMEOUT).times(1)).onNodeEvent(eq("test2"));
        verify(aggMock, timeout(TIMEOUT).times(1)).onNodeEvent(any(TestState.class), eq("test2processedtestValue"));
    }
}
