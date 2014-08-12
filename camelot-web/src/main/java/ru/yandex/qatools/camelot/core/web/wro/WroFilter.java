package ru.yandex.qatools.camelot.core.web.wro;

import ro.isdc.wro.manager.factory.WroManagerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class WroFilter extends ro.isdc.wro.http.WroFilter {
    protected FilterConfig filterConfig;
    protected WroManagerFactory factory;

    @Override
    protected void doInit(FilterConfig config) throws ServletException {
        super.doInit(config);
        this.filterConfig = config;
        if (factory instanceof ConfigurableWroManagerFactory) {
            ((ConfigurableWroManagerFactory) factory).init(filterConfig);
        }
    }

    protected WroManagerFactory newWroManagerFactory() {
        return factory = new ConfigurableWroManagerFactory();
    }
}
