package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginInitializer;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginInitializerImpl implements PluginInitializer {

    public void init(Plugin plugin) throws Exception { //NOSONAR
        new PluginAnnotatedMethodInvoker<>(plugin, OnInit.class).process().invoke();
    }
}
