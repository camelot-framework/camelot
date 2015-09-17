package ru.yandex.qatools.camelot.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

@Aggregate
@FSM(start = UndefinedState.class)
@Filter(instanceOf = StopEvent.class)
@Transitions({
        @Transit(on = StopEvent.class, stop = true),
})
public class BindToOutputAggregator {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @OnTransit
    public void onEvent(UndefinedState state, TestEvent event) {
        logger.warn("=============> ONEVENT {}", event.getMethodname());
        state.setEvent(event);
    }
}
