package ru.yandex.qatools.camelot.maven.web;

import ro.isdc.wro.manager.factory.WroManagerFactory;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class WroFilter extends ru.yandex.qatools.camelot.core.web.wro.WroFilter {
    protected WroManagerFactory newWroManagerFactory() {
        return factory = new ConfigurableWroManagerFactory();
    }
}
