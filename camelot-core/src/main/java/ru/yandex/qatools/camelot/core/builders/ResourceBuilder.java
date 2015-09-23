package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ResourceBuilder {

    /**
     * Build objects and add them to the context
     */
    void build(CamelContext camelContext, Plugin plugin) throws Exception;

}
