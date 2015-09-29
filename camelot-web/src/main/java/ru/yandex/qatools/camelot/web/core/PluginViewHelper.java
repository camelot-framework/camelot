package ru.yandex.qatools.camelot.web.core;

import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.util.MapUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginViewHelper {
    final String pluginId;
    final ViewHelper viewHelper;

    public PluginViewHelper(String pluginId, ViewHelper viewHelper) {
        this.viewHelper = viewHelper;
        this.pluginId = pluginId;
    }

    public String render(String path) throws IOException {
        return render(path, Collections.emptyMap());
    }

    public String render(String path, Map<Object, Object> attributes) throws IOException {
        final PluginContext ctx = viewHelper.pluginsService.getPluginContext(pluginId);
        Map<String, Object> pluginAttrs = viewHelper.getPluginRenderAttrs(ctx);
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            pluginAttrs.put(entry.getKey().toString(), entry.getValue());
        }
        final String resPath = ctx.getResDirPath() + path;
        final String relPath = Paths.get(resPath).normalize().toString();
        return viewHelper.renderSource(relPath, ctx.getClassLoader().getResourceAsStream(relPath), pluginAttrs);
    }

    public Map<Object, Object> values(Object key, Object value, Object... other) {
        return MapUtil.map(key, value, other);
    }
}
