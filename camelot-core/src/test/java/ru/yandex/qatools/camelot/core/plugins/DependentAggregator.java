package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.fsm.annotations.BeforeTransit;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.core.beans.TestState;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;

@Aggregate
@FSM(start = UndefinedState.class)
@Transitions({@Transit(on = TestEvent.class, stop = true)})
public class DependentAggregator {

    @BeforeTransit
    public void beforeTransit(TestState state, TestEvent event) {
        state.setEvent(event);
    }
}
