package ru.yandex.qatools.camelot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.WebfrontEngine;
import ru.yandex.qatools.camelot.core.web.ViewHelper;

import javax.ws.rs.Path;
import java.util.Map;

@Path("/")
@Component
public class IndexResource extends BasicViewResource {

    @Autowired
    ViewHelper viewHelper;

    @Autowired
    WebfrontEngine pluginsEngine;

    @Override
    public Object getTitle() {
        return "Home";
    }

    public Map<String, Plugin> getPlugins() {
        return pluginsEngine.getPluginsMap();
    }

    public PluginContext getPluginContext(String pluginId) {
        return pluginsEngine.getPluginContext(pluginId);
    }

    public ViewHelper getViewHelper() {
        return viewHelper;
    }
}
