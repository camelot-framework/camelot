package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.MessagesSerializer;

import java.util.Map;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginMessageFilter {
    public static final String FILTER_METHOD_NAME = "filter";
    final Plugin plugin;
    final Class customFilterClass;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public PluginMessageFilter(Plugin plugin, Class<? extends CustomFilter> customFilterClass) {
        this.plugin = plugin;
        this.customFilterClass = customFilterClass;
    }

    public boolean filter(@Body Object input, @Headers Map<String, Object> headers) {
        final Object body = process(input, headers);
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
    private Object process(Object body, Map<String, Object> headers) {
        final ClassLoader classLoader = plugin.getContext().getClassLoader();
        final MessagesSerializer serializer = plugin.getContext().getMessagesSerializer();
        return serializer.processBodyAndHeadersAfterReceive(body, headers, classLoader);
    }
}
