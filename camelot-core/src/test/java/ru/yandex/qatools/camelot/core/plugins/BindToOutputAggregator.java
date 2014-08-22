package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

@Aggregate
@FSM(start = UndefinedState.class)
@Filter(instanceOf = StopEvent.class)
@Transitions({
        @Transit(on = StopEvent.class),
})
public class BindToOutputAggregator {
}
