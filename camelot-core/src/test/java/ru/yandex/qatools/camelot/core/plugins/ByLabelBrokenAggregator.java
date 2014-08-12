package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qa.beans.TestBroken;
import ru.yandex.qa.beans.TestEvent;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopByLabelBroken;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.LABEL;
import static ru.yandex.qatools.camelot.util.CloneUtil.deepCopy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = {TestBroken.class, StopByLabelBroken.class})
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestBroken.class),
        @Transit(stop = true, on = StopByLabelBroken.class)
})
public class ByLabelBrokenAggregator {

    @InjectHeaders
    Map<String, Object> headers;

    @ClientSender
    ClientMessageSender sender;

    @ClientSender(topic = "topic2")
    ClientMessageSender sender2;

    @AggregationKey
    public String byLabel(Object event) {
        return (String) headers.get(LABEL);
    }

    @AggregationKey
    public String byLabel(TestEvent event) {
        return event.getConfig().getLabels().get(0);
    }

    @Split
    public List<Object> splitByLabel(TestEvent testEvent) throws IOException {
        List<Object> res = new ArrayList<>();
        for (String label : testEvent.getConfig().getLabels()) {
            final TestEvent newEvent = (TestEvent) deepCopy(testEvent);
            newEvent.getConfig().getLabels().clear();
            newEvent.getConfig().getLabels().add(label);
            res.add(newEvent);
        }
        return res;
    }

    @OnTransit
    public void transit(CounterState newState, TestBroken message) {
        newState.count++;
        newState.label = message.getConfig().getLabels().get(0);
        sender.send("topic1", newState.label);
        sender2.send("hello2");
    }

}
