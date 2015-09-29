package ru.yandex.qatools.camelot.maven.web;

import ro.isdc.wro.manager.factory.WroManagerFactory;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class WroFilter extends ru.yandex.qatools.camelot.web.wro.WroFilter { // NOSONAR
    @Override
    protected WroManagerFactory newWroManagerFactory() {
        return factory = new ConfigurableWroManagerFactory(); //NOSONAR
    }
}
