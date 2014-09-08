package ${groupId}.plugin;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.api.Constants;
import ru.yandex.qatools.camelot.test.*;
import ${groupId}.plugin.*;

import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.test.Matchers.containStateFor;
import static ru.yandex.qatools.matchers.decorators.MatcherDecorators.should;
import static ru.yandex.qatools.matchers.decorators.TimeoutWaiter.timeoutHasExpired;


@RunWith(CamelotTestRunner.class)
public class AggregatorTest {
    @PluginMock(Aggregator.class)
    Aggregator mock;

    @Helper
    TestHelper helper;

    @AggregatorState(Aggregator.class)
    AggregatorStateStorage states;

    @Test
    public void testState() throws IllegalAccessException, InstantiationException, NoSuchAlgorithmException {
        helper.send("test");
        verify(mock, timeout(3000)).onMessage(any(State.class), anyString());
        assertThat(states, should(containStateFor(Constants.Keys.ALL))
                .whileWaitingUntil(timeoutHasExpired(3000)));
    }
}
