package ru.yandex.qatools.camelot.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.api.ClientMessageSender;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.timeoutHasExpired;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseCustomContext("classpath:/test-camelot-custom-context.xml")
public class CamelotTestRunnerCustomTest {

    private static final int TIMEOUT = 5000;

    private static final String STATE_MESSAGE = "test-processed-overridden";

    @PluginMock(id = "test-aggregator")
    TestAggregator aggMock;

    @PluginMock(id = "test-processor")
    TestProcessor prcMock;

    @Helper
    TestHelper helper;

    @AggregatorState(id = "test-aggregator")
    AggregatorStateStorage stateStorage;

    @ClientSenderMock(id = "test-aggregator")
    ClientMessageSender sender;

    @Test
    public void testRoute() throws Exception {
        final String uuid = randomUUID().toString();
        helper.send("test", UUID, uuid);
        verify(prcMock, timeout(TIMEOUT)).onEvent(eq("test"));
        verify(aggMock, timeout(TIMEOUT)).onEvent(any(TestState.class), eq(STATE_MESSAGE));
        verify(sender, timeout(TIMEOUT)).send(any(TestState.class));

        assertThat("State with uuid must exist", stateStorage.get(TestState.class, uuid),
                should(not(nullValue())).whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
    }
}
