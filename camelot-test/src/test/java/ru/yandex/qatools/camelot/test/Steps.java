package ru.yandex.qatools.camelot.test;

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
public class Steps {
    private static final int TIMEOUT = 3000;
    public static final String CHECK_VALUE = "test4-processed-overridden";

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

    @Resource(TestProcessor.class)
    TestResource testResource;

    public void testRoute() throws Exception {
        helper.send("test4", UUID, "uuid1");
        helper.send("test4", UUID, "uuid2");

        verify(prcMock, timeout(TIMEOUT).times(2))
                .onEvent(eq("test4"));
        verify(aggMock, timeout(TIMEOUT).times(2))
                .onEvent(any(TestState.class), eq(CHECK_VALUE));
        verify(sender, timeout(TIMEOUT).times(2))
                .send(any(TestState.class));
        verify(senderComponentTopic, timeout(TIMEOUT).times(2))
                .send(eq(SomeComponent.class.getName()));
        verify(senderInterfaceTopic, timeout(TIMEOUT).times(2))
                .send(eq(SomeComponent.class.getName()));

        verify(aggMock, after(5000).never()).resetState(any(TestState.class));

        TestState state = stateStorage.get(TestState.class, "uuid1");
        assertThat(state, should(having(
                on(TestState.class).getMessage(), equalTo(CHECK_VALUE))
        ).whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
    }
}
