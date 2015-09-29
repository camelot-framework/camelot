package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.RoutesBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface BasicRoutesBuilder extends RoutesBuilder {
    /**
     * Startup routes associated with the plugin from Context
     */
    void removeRoutes() throws Exception; //NOSONAR

    /**
     * Remove routes associated with the plugin from Context
     * Useful while plugins reloading
     */
    void startRoutes() throws Exception; //NOSONAR

}
