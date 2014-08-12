package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qa.beans.TestStarted;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopAggregatorWithTimer;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static java.lang.Thread.sleep;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestStarted.class),
        @Transit(stop = true, on = StopAggregatorWithTimer.class),
})
public class AggregatorWithTimer {
    @InjectHeader(UUID)
    String uuid;

    @Repository
    AggregatorRepository repo;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }

    @OnTransit
    public void transit(CounterState state, TestStarted event) {
        state.label = uuid;
    }

    @OnTimer(cron = "*/2 * * * * ?", skipIfNotCompleted = true, readOnly = false)
    public void skipIfNotCompleted(CounterState state) throws InterruptedException {
        sleep(2000);
        state.count2 += 3;
    }

    @OnTimer(cron = "${timer.everyTwoSeconds}", readOnly = false)
    public void everyTwoSeconds(CounterState state) {
        state.count++;
    }

    @OnTimer(cronMethod = "timerExpr", readOnly = false)
    public void everySecond(CounterState state) {
        state.count2++;
    }

    public String timerExpr(String method) {
        if (repo == null) {
            throw new RuntimeException("context must be injected");
        }
        if (method.equals("everyTwoSeconds")) {
            return "*/2 * * * * ?";
        }
        return "* * * * * ?";
    }

}
