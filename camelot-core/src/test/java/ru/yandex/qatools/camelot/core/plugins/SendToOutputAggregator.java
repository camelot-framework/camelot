package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

@Aggregate
@FSM(start = UndefinedState.class)
@Filter(instanceOf = StopEvent.class)
@Transitions({
        @Transit(on = StopEvent.class, to = StopEvent.class, stop = true),
})
public class SendToOutputAggregator {

    @Output
    EventProducer output;

    @OnTransit
    public void onEvent(Object state, Object event) {
        output.produce(event);
    }
}
