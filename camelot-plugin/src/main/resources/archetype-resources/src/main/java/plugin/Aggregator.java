package ${groupId}.plugin;

import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.OnTransit;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = String.class)
@Transitions({
        @Transit(on = String.class),
})
public class Aggregator {
    @Output
    EventProducer out;

    @OnTransit
    public void onMessage(String state, String message){
        // stub
    }
}
