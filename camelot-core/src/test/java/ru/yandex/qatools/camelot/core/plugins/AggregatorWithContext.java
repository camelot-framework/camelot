package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopAggregatorWithTimer;
import ru.yandex.qatools.camelot.core.beans.TestStarted;
import ru.yandex.qatools.camelot.core.impl.InjectableComponent;
import ru.yandex.qatools.camelot.core.impl.InjectableInterface;
import ru.yandex.qatools.camelot.core.impl.InjectableInterfaceImpl;
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
public class AggregatorWithContext {
    @InjectHeader(UUID)
    public String uuid;

    @InjectHeaders
    public Map<String, Object> headers;

    @Repository
    public AggregatorRepository repo1;

    @Repository(AllSkippedAggregator.class)
    public AggregatorRepository repo2;

    @Repository(id = "all-skipped")
    public AggregatorRepository repo3;

    @Listener
    public EndpointListener listener;

    @Config
    public AppConfig config;

    @PluginStorage
    public Storage storage;

    @PluginStorage(AllSkippedAggregator.class)
    public Storage storage1;

    @PluginStorage(id = "all-skipped")
    public Storage storage2;

    @Input
    public EventProducer input1;

    @Input(AllSkippedAggregator.class)
    public EventProducer input2;

    @Input(id = "all-skipped")
    public EventProducer input3;

    @Output
    public EventProducer output1;

    @Output(AllSkippedAggregator.class)
    public EventProducer output2;

    @Output(id = "all-skipped")
    public EventProducer output3;

    @ClientSender
    public ClientMessageSender clientSender;

    @Plugins
    public PluginsInterop plugins;

    @Plugin(AllSkippedAggregator.class)
    public PluginInterop plugin1;

    @Plugin(id = "all-skipped")
    public PluginInterop plugin2;

    @ConfigValue("custom.plugin.stringValue")
    public String configValue;

    @PluginComponent
    public InjectableComponent injectableComponent;

    @PluginComponent(impl = InjectableInterfaceImpl.class)
    public InjectableInterface injectableInterface;

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }
}
