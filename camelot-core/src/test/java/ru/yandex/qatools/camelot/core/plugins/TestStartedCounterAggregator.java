package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qa.beans.TestEvent;
import ru.yandex.qa.beans.TestStarted;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopTestStartedCounter;
import ru.yandex.qatools.camelot.core.impl.TestEventUtil;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {TestStarted.class, StopTestStartedCounter.class})
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestStarted.class),
        @Transit(on = StopTestStartedCounter.class, stop = true),
})
public class TestStartedCounterAggregator {

    @OnTransit
    public void transit(CounterState newState, TestStarted message) {
        newState.count++;
    }

    @AggregationKey
    public String byTestMethod(TestEvent event) {
        return TestEventUtil.methodFullName(event);
    }
}
