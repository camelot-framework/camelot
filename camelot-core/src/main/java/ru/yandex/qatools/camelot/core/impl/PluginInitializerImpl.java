package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.PluginInitializer;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginInitializerImpl implements PluginInitializer {

    public void init(Plugin plugin) throws Exception {
        new PluginAnnotatedMethodInvoker<>(plugin, OnInit.class).process().invoke();
    }
}
