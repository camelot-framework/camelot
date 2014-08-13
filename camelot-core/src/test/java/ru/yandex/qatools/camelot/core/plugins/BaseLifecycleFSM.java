package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.TestDropped;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.camelot.core.beans.TestFailure;
import ru.yandex.qatools.camelot.core.beans.TestPassed;
import ru.yandex.qatools.camelot.core.beans.TestStarted;
import ru.yandex.qatools.camelot.core.beans.UndefinedTestEvent;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.NewState;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

@Aggregate
@FSM(start = UndefinedTestEvent.class)
@Transitions({
        @Transit(from = UndefinedTestEvent.class, to = TestEvent.class, on = TestEvent.class),
        @Transit(from = TestStarted.class, to = TestEvent.class, on = {TestPassed.class, TestFailure.class}, stop = true),
        @Transit(from = {TestPassed.class, TestFailure.class}, on = TestStarted.class, stop = true),
        @Transit(to = StopEvent.class, on = {StopEvent.class, TestDropped.class}, stop = true)
})
public abstract class BaseLifecycleFSM {
    @InjectHeader(UUID)
    String uuid;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }

    @OnTransit
    public void onTestCompleted(TestEvent oldState, TestEvent newState, TestEvent event) {
        if (event instanceof TestStarted) {
            event.setTime(oldState.getTime() - event.getTime());
        } else {
            event.setTime(event.getTime() - oldState.getTime());
        }
    }

    @NewState
    public <T> Object initState(Class<T> stateClass, TestEvent event) throws IllegalAccessException, InstantiationException {
        if (event != null) {
            event.setTimestamp(event.getTime()); // store the original timestamp
            return event;
        }
        return stateClass.newInstance();
    }
}
