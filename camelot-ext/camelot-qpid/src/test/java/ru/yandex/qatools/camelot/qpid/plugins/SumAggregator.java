package ru.yandex.qatools.camelot.qpid.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ilya Sadykov
 */
@Aggregate
@FSM(start = AtomicInteger.class)
@Transitions(@Transit(on = Integer.class))
public class SumAggregator {

    @OnTransit
    public void onInteger(AtomicInteger result, Integer value) {
        result.set(result.get() + value);
    }
}
