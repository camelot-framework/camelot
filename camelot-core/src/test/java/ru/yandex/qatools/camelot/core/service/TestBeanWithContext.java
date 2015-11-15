package ru.yandex.qatools.camelot.core.service;

import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedAggregator;

/**
 * @author Ilya Sadykov
 */
public class TestBeanWithContext {

    @Repository(AllSkippedAggregator.class)
    public AggregatorRepository repo2;

    @Repository(id = "all-skipped")
    public AggregatorRepository repo3;

    @Config
    public AppConfig config;

    @PluginStorage(AllSkippedAggregator.class)
    public Storage storage1;

    @PluginStorage(id = "all-skipped")
    public Storage storage2;

    @MainInput
    public EventProducer mainInput;

    @Input(AllSkippedAggregator.class)
    public EventProducer input2;

    @Input(id = "all-skipped")
    public EventProducer input3;

    @Output(AllSkippedAggregator.class)
    public EventProducer output2;

    @Output(id = "all-skipped")
    public EventProducer output3;

    @Plugins
    public PluginsInterop plugins;

    @Plugin(AllSkippedAggregator.class)
    public PluginInterop plugin1;

    @Plugin(id = "all-skipped")
    public PluginInterop plugin2;

    @ConfigValue("custom.plugin.stringValue")
    public String configValue;
}
