package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.core.beans.CollectEventsState;
import ru.yandex.qatools.camelot.core.beans.FilteredEvent;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@Filter(custom = CustomFilteredAggregator.class)
@FSM(start = CollectEventsState.class)
@Transitions({
        @Transit(on = FilteredEvent.class, stop = true),
        @Transit(on = Object.class)
})
public class CustomFilteredAggregator implements CustomFilter {
    @OnTransit
    public void transit(CollectEventsState state, Object event) {
        state.collected.add(event);
    }

    @Override
    public boolean filter(Object body) {
        return body instanceof FilteredEvent;
    }
}
