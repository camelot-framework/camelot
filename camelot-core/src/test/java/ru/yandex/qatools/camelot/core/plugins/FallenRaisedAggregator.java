package ru.yandex.qatools.camelot.core.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qa.beans.TestDropped;
import ru.yandex.qa.beans.TestEvent;
import ru.yandex.qa.beans.TestFailure;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.camelot.core.beans.UndefinedTestEvent;
import ru.yandex.qatools.camelot.core.impl.TestEventUtil;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = UndefinedTestEvent.class)
@Transitions({
        @Transit(from = UndefinedTestEvent.class, to = TestEvent.class, on = TestEvent.class),
        @Transit(on = TestDropped.class, stop = true)
})
public class FallenRaisedAggregator extends LifecycleFSM {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Output
    EventProducer producer;

    @AggregationKey
    public String byTestMethod(TestEvent event) {
        return TestEventUtil.methodFullName(event);
    }

    @OnTransit
    public void onTestFailureEvent(TestFailure event) {
        logger.info("Producing message with header 'method'=" + event.getMethodname());
        producer.produce(event.getMethodname(), "method", event.getMethodname());
    }
}
