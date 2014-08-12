package ru.yandex.qatools.camelot.test;

import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.fsm.annotations.*;
import ru.yandex.qatools.camelot.api.ClientMessageSender;

import static jodd.util.StringUtil.isEmpty;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {Float.class, String.class})
@Aggregate
@FSM(start = TestState.class)
@Transitions({@Transit(on = String.class), @Transit(stop = true, on = Float.class)})
public class TestAggregator {
    @InjectHeader(UUID)
    String uuid;

    @ClientSender
    ClientMessageSender sender;

    @ClientSender(topic = "test")
    ClientMessageSender senderTopic;

    @PluginComponent
    SomeComponent someComponent;

    @ConfigValue("camelot-test.property.mustExists")
    String mustExistProperty = null;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }

    @OnTransit
    public void onNodeEvent(TestState state, String event) {
        if (isEmpty(mustExistProperty)) {
            throw new RuntimeException("Property must exist!");
        }
        if (event == null) {
            throw new RuntimeException("Got null event!");
        }
        state.setMessage(event);
        sender.send(state);
        senderTopic.send(someComponent.getClass().getName());
    }

    @OnTransit
    public void onStop(TestState state, Float event) {
        state.setMessage(String.valueOf(event));
    }

    @OnTimer(cron = "* * * * * ?", readOnly = false)
    public void resetState(TestState state) {
        state.setMessage(null);
    }

    @NewState
    public Object initState(Class stateClass) {
        if (isEmpty(mustExistProperty)) {
            throw new RuntimeException("Property must exist!");
        }
        return new TestState();
    }
}
