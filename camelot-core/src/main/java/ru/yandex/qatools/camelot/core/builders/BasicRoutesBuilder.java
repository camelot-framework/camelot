package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.RoutesBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface BasicRoutesBuilder extends RoutesBuilder {
    /**
     * Startup routes associated with the plugin from Context
     */
    void removeRoutes() throws Exception;

    /**
     * Remove routes associated with the plugin from Context
     * Useful while plugins reloading
     */
    void startRoutes() throws Exception;

}
