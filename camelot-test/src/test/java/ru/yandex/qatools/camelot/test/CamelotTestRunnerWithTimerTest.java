package ru.yandex.qatools.camelot.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.matchers.decorators.MatcherDecorators;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.camelot.test.Matchers.containStateFor;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseProperties("camelot-test.properties")
@DisableTimers
public class CamelotTestRunnerWithTimerTest {

    private static final String KEY = "uuid1";

    private static final int TIMEOUT = 3000;

    @PluginMock
    TestAggregator aggMock;

    @PluginMock
    TestProcessor prcMock;

    @Helper
    TestHelper helper;

    @AggregatorState(TestAggregator.class)
    AggregatorStateStorage stateStorage;

    @Test
    public void testRoute() throws Exception {
        helper.send("test", UUID, KEY);

        verify(prcMock, timeout(TIMEOUT).times(1)).onEvent(eq("test"));
        verify(aggMock, timeout(TIMEOUT).times(1)).onEvent(any(TestState.class), eq("testprocessedtestValue"));

        assertThat(stateStorage, should(containStateFor(KEY))
                .whileWaitingUntil(timeoutHasExpired(TIMEOUT)));

        helper.invokeTimersFor(TestAggregator.class);
        verify(aggMock, times(1)).resetState(any(TestState.class));
        TestState state = stateStorage.get(TestState.class, KEY);
        assertThat(state, should(having(on(TestState.class).getMessage(), nullValue()))
                .whileWaitingUntil(MatcherDecorators.timeoutHasExpired(TIMEOUT)));
    }
}
