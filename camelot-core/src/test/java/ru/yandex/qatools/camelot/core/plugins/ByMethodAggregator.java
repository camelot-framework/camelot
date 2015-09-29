package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.core.beans.*;
import ru.yandex.qatools.camelot.core.impl.TestEventUtil;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = UndefinedState.class)
@Transitions({
        @Transit(on = TestFailed.class, to = TestFailedState.class),
        @Transit(on = TestPassed.class, to = TestPassedState.class, stop = true),
})
public class ByMethodAggregator {

    @AggregationKey
    public String byTestMethod(TestEvent event) {
        return TestEventUtil.methodFullName(event);
    }
}
