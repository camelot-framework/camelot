package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.qatools.fsm.annotations.*;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@FSM(start = TestStateMachine.UndefinedState.class)
@Transitions({
        @Transit(from = TestStateMachine.UndefinedState.class, to = TestStateMachine.ProgressState.class, on = TestStateMachine.TStartProgress.class),
        @Transit(from = TestStateMachine.ProgressState.class, to = TestStateMachine.FinishedState.class, on = TestStateMachine.TFinishProgress.class, stop = true),
})
public class TestStateMachine {
    @Autowired
    TestComponent testComponent;

    @Produce(uri = "seda:queue:done")
    private ProducerTemplate doneQueue;

    public static abstract class TState implements Serializable {
        TEvent event;
    }

    public static class UndefinedState extends TState {

    }

    public static class ProgressState extends TState {

    }

    public static class FinishedState extends TState {

    }

    public static abstract class TEvent implements Serializable {
        String uuid;

        protected TEvent(String uuid) {
            this.uuid = uuid;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }

    public static class TStartProgress extends TEvent {
        public TStartProgress(String uuid) {
            super(uuid);
        }
    }

    public static class TFinishProgress extends TEvent {
        public TFinishProgress(String uuid) {
            super(uuid);
        }
    }

    @OnTransit
    public void entryFinishedState(FinishedState newState, TFinishProgress event) {
        if (testComponent == null || doneQueue == null) {
            throw new RuntimeException("Framework must autowire Camel and spring fields!");
        }
    }

    @NewState
    public TState initState(Class<? extends TState> stateClass, TEvent event) throws Exception {
        TState res = stateClass.newInstance();
        res.event = event;
        return res;
    }

}
