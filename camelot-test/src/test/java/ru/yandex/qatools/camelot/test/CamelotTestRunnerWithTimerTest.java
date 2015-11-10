package ru.yandex.qatools.camelot.test;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;
import static ru.yandex.qatools.camelot.test.Matchers.containStateFor;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@RunWith(CamelotTestRunner.class)
@UseProperties("camelot-test-custom.properties")
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

    private TestState state;

    @Before
    public void initAggregator() {
        helper.send("test", UUID, KEY);
        verify(prcMock, timeout(TIMEOUT))
                .onEvent(eq("test"));
        verify(aggMock, timeout(TIMEOUT))
                .onEvent(any(TestState.class), eq("test-processed-test-value"));
        assertThat(stateStorage, should(containStateFor(KEY))
                .whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
        state = stateStorage.get(TestState.class, KEY);
    }

    @Test
    public void testInvokingAllTimers() {
        helper.invokeTimersFor(TestAggregator.class);

        verifyResetStateCalled(1);
        verifySetFlagCalled(1);
        assertMessage(nullValue());
        assertFlag(true);
    }

    @Test
    public void testInvokingResetStateTimerOnly() {
        helper.invokeTimerFor(TestAggregator.class, "resetState");

        verifyResetStateCalled(1);
        verifySetFlagCalled(0);
        assertMessage(nullValue());
        assertFlag(false);
    }

    @Test
    public void testInvokingSetFlagTimerOnly() {
        helper.invokeTimerFor(TestAggregator.class, "setFlag");

        verifyResetStateCalled(0);
        verifySetFlagCalled(1);
        assertMessage(notNullValue());
        assertFlag(true);
    }

    @Test(expected = NullPointerException.class)
    public void testInvokingNotExistentTimer() throws Throwable {
        try {
            helper.invokeTimerFor(TestAggregator.class, "blkjhsdgfksdyufgks");
        } catch (RuntimeException e) {
            assertThat(e.getClass().getName(), is(RuntimeException.class.getName()));
            throw e.getCause();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testInvokingTimerWithNullMethodName() throws Throwable {
        try {
            helper.invokeTimerFor(TestAggregator.class, null);
        } catch (RuntimeException e) {
            assertThat(e.getClass().getName(), is(RuntimeException.class.getName()));
            throw e.getCause();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testInvokingTimerWithEmptyMethodName() throws Throwable {
        try {
            helper.invokeTimerFor(TestAggregator.class, "");
        } catch (RuntimeException e) {
            assertThat(e.getClass().getName(), is(RuntimeException.class.getName()));
            throw e.getCause();
        }
    }

    private void verifyResetStateCalled(int numberOfInvocations) {
        verify(aggMock, times(numberOfInvocations)).resetState(any(TestState.class));
    }

    private void verifySetFlagCalled(int numberOfInvocations) {
        verify(aggMock, times(numberOfInvocations)).setFlag(any(TestState.class));
    }

    private void assertMessage(Matcher<Object> messageMatcher) {
        assertThat(state, should(having(on(TestState.class).getMessage(), messageMatcher))
                .whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
    }

    private void assertFlag(boolean flagSet) {
        assertThat(state, should(having(on(TestState.class).isCronFlagSet(), is(flagSet)))
                .whileWaitingUntil(timeoutHasExpired(TIMEOUT)));
    }
}
