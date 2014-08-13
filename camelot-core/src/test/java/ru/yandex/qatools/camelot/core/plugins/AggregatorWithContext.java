package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.EndpointListener;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginInterop;
import ru.yandex.qatools.camelot.api.PluginsInterop;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.Aggregate;
import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.ClientSender;
import ru.yandex.qatools.camelot.api.annotations.Config;
import ru.yandex.qatools.camelot.api.annotations.ConfigValue;
import ru.yandex.qatools.camelot.api.annotations.InjectHeader;
import ru.yandex.qatools.camelot.api.annotations.InjectHeaders;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.Listener;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.camelot.api.annotations.Plugin;
import ru.yandex.qatools.camelot.api.annotations.PluginComponent;
import ru.yandex.qatools.camelot.api.annotations.PluginStorage;
import ru.yandex.qatools.camelot.api.annotations.Plugins;
import ru.yandex.qatools.camelot.api.annotations.Repository;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopAggregatorWithTimer;
import ru.yandex.qatools.camelot.core.beans.TestStarted;
import ru.yandex.qatools.camelot.core.impl.InjectableComponent;
import ru.yandex.qatools.fsm.annotations.FSM;
import ru.yandex.qatools.fsm.annotations.Transit;
import ru.yandex.qatools.fsm.annotations.Transitions;

import java.util.Map;

import static ru.yandex.qatools.camelot.api.Constants.Headers.UUID;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
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

    @AggregationKey
    public String byUuid(Object event) {
        return uuid;
    }
}
