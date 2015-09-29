package ru.yandex.qatools.camelot.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.config.PluginWebContext;
import ru.yandex.qatools.camelot.web.core.ViewHelper;
import ru.yandex.qatools.camelot.web.core.WebfrontEngine;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Component
@Scope("request")
@Path("/plugin/{plugin}")
public class PluginIndexResource extends BasicViewResource {

    @Autowired
    ViewHelper viewHelper;

    @Autowired
    WebfrontEngine pluginsService;

    @PathParam("plugin")
    String pluginId;

    @Override
    public Object getTitle() {
        return "Plugin " + pluginId;
    }

    public PluginWebContext getPlugin() {
        return pluginsService.getPluginContext(pluginId);
    }

    public ViewHelper getViewHelper() {
        return viewHelper;
    }

    public String getPluginId() {
        return pluginId;
    }
}
