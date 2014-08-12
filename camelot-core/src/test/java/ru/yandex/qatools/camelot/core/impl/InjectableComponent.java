package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.PluginStorage;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedAggregator;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class InjectableComponent {

    @Input
    EventProducer input;

    @PluginStorage(AllSkippedAggregator.class)
    Storage otherPluginStorage;

}
