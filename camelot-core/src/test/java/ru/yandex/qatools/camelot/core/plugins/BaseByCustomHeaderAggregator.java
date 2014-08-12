package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qa.beans.TestEvent;
import ru.yandex.qa.beans.TestFailed;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.Constants;
import ru.yandex.qatools.camelot.api.PluginInterop;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.beans.CounterState;
import ru.yandex.qatools.camelot.core.beans.StopByCustomHeader;
import ru.yandex.qatools.fsm.annotations.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Aggregate
@FSM(start = CounterState.class)
@Transitions({
        @Transit(on = TestEvent.class),
        @Transit(stop = true, on = StopByCustomHeader.class)
})
public abstract class BaseByCustomHeaderAggregator {

    @InjectHeader(Constants.Headers.CORRELATION_KEY)
    String correlationKey;

    @InjectHeader("customHeader")
    String customHeader;

    @PluginStorage
    Storage<Object> storage;

    @Plugin(id = "dependent")
    PluginInterop dependent;

    @ConfigValue("custom.plugin.stringValue")
    String stringValue = "default";

    @ConfigValue("custom.plugin.doubleValue")
    double doubleValue = 1.0;

    @ConfigValue("custom.plugin.booleanValue")
    boolean booleanValue = false;

    @Config
    AppConfig appConfig;

    @AggregationKey
    public String byCustomHeader(Object event) {
        return customHeader;
    }

    @OnTransit
    public void transit(CounterState state, TestFailed event) {
        state.count++;
        storage.put("count", state.count);
        storage.put("string", stringValue);
        storage.put("double", doubleValue);
        storage.put("boolean", booleanValue);
        storage.put("propertyFromAppConfig", appConfig.getProperty("custom.plugin.stringValue"));
    }

    @BeforeTransit
    public void beforeTransit(CounterState state, TestEvent event) {
        state.label = customHeader;
        state.label2 = correlationKey;
        dependent.input().produce(event);
    }
}
