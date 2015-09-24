package ru.yandex.qatools.camelot.common;

import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.api.annotations.Split;
import ru.yandex.qatools.camelot.beans.RouteConfig;
import ru.yandex.qatools.camelot.beans.RouteConfigImpl;
import ru.yandex.qatools.camelot.config.PluginContext;

import static ru.yandex.qatools.camelot.util.ReflectUtil.getAnnotationValue;
import static ru.yandex.qatools.camelot.util.ReflectUtil.getAnnotationWithinHierarchy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class RouteConfigReader {
    private final PluginContext pluginContext;

    public RouteConfigReader(PluginContext context) {
        this.pluginContext = context;
    }

    @SuppressWarnings("unchecked")
    public RouteConfig read() throws ReflectiveOperationException {
        RouteConfigImpl config = new RouteConfigImpl();
        final Class classToRead = pluginContext.getClassLoader().loadClass(pluginContext.getPluginClass());
        if (Metadata.getMeta(classToRead).getAnnotatedMethods(Split.class).length > 0) {
            config.setSplitStrategy(new PluginMethodSplitStrategy(pluginContext));
        }
        Object filter = getAnnotationWithinHierarchy(classToRead, Filter.class);
        if (filter != null) {
            final Class<? extends CustomFilter> custom = (Class<? extends CustomFilter>) getAnnotationValue(filter, "custom");
            if (custom != null && !custom.equals(CustomFilter.class)) {
                config.setCustomFilter(custom);
            } else {
                config.setFilterInstanceOf((Class[]) getAnnotationValue(filter, "instanceOf"));
            }
        }
        return config;
    }
}
