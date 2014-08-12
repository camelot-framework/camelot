package ru.yandex.qatools.camelot.features;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.ProcessingEngine;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Map;
import java.util.Set;

import static ru.yandex.qatools.camelot.util.NameUtil.pluginResourceBeanName;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 16.07.14
 */
public class LoadPluginResourceFeature implements Feature {

    @Override
    public boolean configure(FeatureContext context) {
        final WebApplicationContext appContext = ContextLoader.getCurrentWebApplicationContext();
        Set<Map.Entry<String, Plugin>> plugins = appContext.getBean(ProcessingEngine.class).getPluginsMap().entrySet();

        for (Map.Entry<String, Plugin> entry : plugins) {
            Plugin plugin = entry.getValue();
            if (plugin.getResource() != null) {
                context.register(appContext.getBean(pluginResourceBeanName(plugin.getId())));
            }
        }

        return true;
    }
}
