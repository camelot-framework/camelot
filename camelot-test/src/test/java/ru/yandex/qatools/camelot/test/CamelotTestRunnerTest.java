package ru.yandex.qatools.camelot.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.api.ClientMessageSender;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.timeoutHasExpired;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseProperties("camelot-test-custom.properties")
@DisableTimers
@CamelotTestConfig(components = {
        @OverrideComponent(from = SomeComponent.class, to = AnotherComponent.class),
        @OverrideComponent(from = SomeInterface.class, to = AnotherComponent.class)
})
public class CamelotTestRunnerTest {

    private static final int TIMEOUT = 3000;
    private static final String CHECK_VALUE = "test1-processed-test-value";

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

    @ClientSenderMock(value = TestAggregator.class, topic = "someComponent")
    ClientMessageSender senderComponentTopic;

    @ClientSenderMock(value = TestAggregator.class, topic = "someInterface")
    ClientMessageSender senderInterfaceTopic;

    @Test
    public void testRoute() throws Exception {
        helper.send("test1", UUID, "uuid1");
        helper.send("test1", UUID, "uuid2");

        verify(prcMock, timeout(TIMEOUT).times(2)).onEvent(eq("test1"));
        verify(aggMock, timeout(TIMEOUT).times(2)).onEvent(any(TestState.class), eq(CHECK_VALUE));
        verify(sender, timeout(TIMEOUT).times(2)).send(any(TestState.class));
        verify(senderComponentTopic, timeout(TIMEOUT).times(2)).send(eq(AnotherComponent.class.getName()));
        verify(senderInterfaceTopic, timeout(TIMEOUT).times(2)).send(eq(AnotherComponent.class.getName()));

        verify(aggMock, after(5000).never()).resetState(any(TestState.class));

        TestState state = stateStorage.get(TestState.class, "uuid1");
        assertThat(state, should(having(
                on(TestState.class).getMessage(), equalTo(CHECK_VALUE))
        ).whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
    }

    @Test
    public void testRouteAgain() throws Exception {
        helper.send("test2", UUID, "uuid3");
        verify(prcMock, timeout(TIMEOUT).times(1)).onEvent(eq("test2"));
        verify(aggMock, timeout(TIMEOUT).times(1)).onEvent(any(TestState.class), eq("test2-processed-test-value"));
    }
}
