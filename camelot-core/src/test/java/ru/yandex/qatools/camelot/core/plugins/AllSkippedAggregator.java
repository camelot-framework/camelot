package ru.yandex.qatools.camelot.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qa.beans.TestSkipped;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopAllSkipped;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static ru.yandex.qatools.camelot.api.Constants.Keys.ALL;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestSkipped.class),
        @Transit(stop = true, on = StopAllSkipped.class)
})
public class AllSkippedAggregator {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @AggregationKey
    public String all(Object event) {
        return ALL;
    }

    @Output
    EventProducer output;

    @OnTransit
    public void transit(CounterState newState, TestSkipped message) {
        logger.info("Transit " + message);
        newState.count++;
    }
}
