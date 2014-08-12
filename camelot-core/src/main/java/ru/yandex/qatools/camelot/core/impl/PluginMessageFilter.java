package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Body;
import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.Constants;
import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.config.Plugin;

import java.io.Serializable;

import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginMessageFilter {
    public static final String FILTER_METHOD_NAME = "filter";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final Plugin plugin;
    final Class customFilterClass;

    public PluginMessageFilter(Plugin plugin, Class<? extends CustomFilter> customFilterClass) {
        this.plugin = plugin;
        this.customFilterClass = customFilterClass;
    }

    public boolean filter(@Body Object input, @Header(Constants.Headers.BODY_CLASS) String bodyClass) {
        final Object body = process(input, bodyClass);
        try {
            CustomFilter filter = (CustomFilter) customFilterClass.newInstance();
            plugin.getContext().getInjector().inject(filter, plugin.getContext());
            return body != null && filter.filter(body);
        } catch (Exception e) {
            logger.warn("Failed to instantiate the filter class for plugin " + plugin.getId(), e);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Object process(Object body, String bodyClass) {
        if (body instanceof byte[]) {
            try {
                final ClassLoader classLoader = plugin.getContext().getClassLoader();
                body = deserializeFromBytes((byte[]) body, classLoader, (Class<? extends Serializable>) classLoader.loadClass(bodyClass));
            } catch (Exception e) {
                logger.debug("Failed to deserialize event object", e);
            }
        }
        return body;
    }
}
