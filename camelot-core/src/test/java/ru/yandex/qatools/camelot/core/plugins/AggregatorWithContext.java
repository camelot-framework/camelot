package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopAggregatorWithTimer;
import ru.yandex.qatools.camelot.core.beans.TestStarted;
import ru.yandex.qatools.camelot.core.impl.InjectableComponent;
import ru.yandex.qatools.camelot.core.impl.InjectableInterface;
import ru.yandex.qatools.camelot.core.impl.InjectableInterfaceImpl;
import ru.yandex.qatools.camelot.core.service.TestBeanWithContext;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestStarted.class),
        @Transit(stop = true, on = StopAggregatorWithTimer.class),
})
public class AggregatorWithContext extends TestBeanWithContext {
    @InjectHeader(UUID)
    public String uuid;

    @InjectHeaders
    public Map<String, Object> headers;

    @Repository
    public AggregatorRepository repo;

    @PluginStorage
    public Storage storage;

    @Input
    public EventProducer input;

    @Output
    public EventProducer output;

    @ClientSender
    public ClientMessageSender clientSender;

    @Plugins
    public PluginsInterop plugins;

    @PluginComponent
    public InjectableComponent injectableComponent;

    @PluginComponent(impl = InjectableInterfaceImpl.class)
    public InjectableInterface injectableInterface;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }
}
