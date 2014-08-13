package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.core.beans.TestFailed;
import ru.yandex.qatools.camelot.core.beans.TestFailedState;
import ru.yandex.qatools.camelot.core.beans.TestPassed;
import ru.yandex.qatools.camelot.core.beans.TestPassedState;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = UndefinedState.class)
@Transitions({
        @Transit(on = TestFailed.class, to = TestFailedState.class),
        @Transit(on = TestPassed.class, to = TestPassedState.class, stop = true),
})
public abstract class BaseByCustomStrategyAggregator {
    @InjectHeader(CORRELATION_KEY)
    String correlationKey;

    @OnTransit
    public void onTestPassed(TestPassedState newState, TestPassed event) {
        if (!correlationKey.equals(event.getClassname())) {
            throw new RuntimeException(String.format("Correlation key expected <%s>  but was <%s>",
                    event.getClassname(), correlationKey));
        }
        newState.setEvent(event);
    }

    @AggregationKey
    public String byTestFailed(TestFailed event) {
        return event.getClassname();
    }

    @AggregationKey
    public String byTestPassed(TestPassed event) {
        return event.getClassname();
    }
}
