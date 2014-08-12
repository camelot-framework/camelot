package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.InitEvent;

@Aggregate
@FSM(start = CounterState.class)
@Filter(instanceOf = {InitEvent.class})
@Transitions({
        @Transit(on = InitEvent.class),
})
public class InitializableAggregator {

    @Output
    EventProducer output;

    @Input
    EventProducer input;

    @OnTransit
    public void onInit(CounterState state, InitEvent event) {
        state.count++;
        state.label = event.getLabel();
        output.produce(state);
    }

    @OnInit
    public void init() {
        input.produce(new InitEvent("test"));
    }
}
