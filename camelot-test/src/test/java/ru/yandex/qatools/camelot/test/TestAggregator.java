package ru.yandex.qatools.camelot.test;

import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.fsm.annotations.*;

import static jodd.util.StringUtil.isEmpty;
import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
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

    @ClientSender(topic = "someComponent")
    ClientMessageSender senderComponentTopic;

    @ClientSender(topic = "someInterface")
    ClientMessageSender senderInterfaceTopic;

    @PluginComponent
    SomeComponent someComponent;

    @PluginComponent(impl = SomeComponent.class)
    SomeInterface someInterface;

    @ConfigValue("camelot-test.property.mustExists")
    String mustExistProperty = null;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }

    @OnTransit
    public void onEvent(TestState state, String event) {
        if (isEmpty(mustExistProperty)) {
            throw new RuntimeException("Property must exist!");
        }
        if (event == null) {
            throw new RuntimeException("Got null event!");
        }
        state.setMessage(event);
        sender.send(state);
        senderComponentTopic.send(someComponent.getClass().getName());
        senderInterfaceTopic.send(someInterface.getClass().getName());
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
