package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopByHourOfDay;
import ru.yandex.qatools.camelot.core.beans.TestBroken;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static java.util.Calendar.HOUR_OF_DAY;
import static ru.yandex.qatools.camelot.util.DateUtil.calThen;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {TestBroken.class, StopByHourOfDay.class})
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestBroken.class),
        @Transit(stop = true, on = StopByHourOfDay.class)
})
public class ByHourOfDayAggregator {

    @AggregationKey
    public String byHourOfDay(TestEvent event) {
        return String.valueOf(calThen(event.getTimestamp()).get(HOUR_OF_DAY));
    }

    @OnTransit
    public void transit(CounterState newState, TestBroken event) {
        if (event.getConfig().getLabels().size() > 0) {
            newState.label = event.getConfig().getLabels().get(0);
        }
        newState.count++;
    }
}
