package ru.yandex.qatools.camelot.core.plugins;

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
@Filter(instanceOf = FilteredEvent.class)
@FSM(start = CollectEventsState.class)
@Transitions({
        @Transit(on = FilteredEvent.class, stop = true),
        @Transit(on = Object.class)
})
public abstract class BaseFilteredAggregator {
    @OnTransit
    public void transit(CollectEventsState state, Object event) {
        state.collected.add(event);
    }
}
