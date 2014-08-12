package ru.yandex.qatools.camelot.core.web.wro;

import jodd.util.Wildcard;
import org.apache.commons.io.IOUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.WebfrontEngine;
import ru.yandex.qatools.camelot.error.PluginsSystemException;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.*;

import static java.io.File.separator;
import static ro.isdc.wro.util.StringUtils.cleanPath;
import static ru.yandex.qatools.camelot.util.PluginContextTemplateUtil.replaceCss;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginsResourceLocator extends ClasspathUriLocator {

    public static final String MULTI_PREFIX = "plugins://";
    public static final String PLUGIN_ID_REGEX = "([^/]+)/.*";
    public static final String ALIAS = "pluginsResourceLocator";

    protected final WebfrontEngine pluginsEngine;
    protected final Map<String, Plugin> pluginsCache = new LinkedHashMap<>();
    protected final WebApplicationContext context;
    protected final ServletContext servletContext;
    protected final String pluginContextPath;

    public PluginsResourceLocator() {
        this.servletContext = Context.get().getServletContext();
        this.context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        pluginsEngine = context.getBean(WebfrontEngine.class);
        pluginContextPath = servletContext.getContextPath() + "/plugin/";
        fillResources();
    }

    @Override
    public InputStream locate(String uri) throws IOException {
        try {
            if (uri.trim().startsWith(MULTI_PREFIX + pluginContextPath)) {
                return locateSingle(uri);
            }
            return locateMulti(uri);
        } catch (Exception e) {
            throw new PluginsSystemException("Failed to locate uri within the plugins: " + uri, e);
        }
    }

    protected InputStream locateSingle(String uri) throws Exception {
        final String location = cleanPath(uri.replaceFirst(MULTI_PREFIX + pluginContextPath, "")).trim();
        final String pluginId = location.replaceFirst(PLUGIN_ID_REGEX, "$1");
        final String pluginPath = location.replaceFirst(pluginId + "/", "");
        final Plugin plugin = pluginsEngine.getPlugin(pluginId);
        if (plugin != null) {
            final String path = plugin.getContext().getResDirPath() + pluginPath;
            final StringWriter writer = new StringWriter();
            IOUtils.copy(new InputStreamReader(getInputStream(plugin, path)), writer);
            String relPath = "";
            final File parentPath = new File(pluginPath).getParentFile();
            if (parentPath != null) {
                relPath = separator + parentPath.getPath();
            }
            final String replacedRes = processResource(writer.toString(), plugin, relPath);
            return IOUtils.toInputStream(replacedRes);
        }
        return null;
    }

    protected InputStream locateMulti(String uri) throws Exception {
        Map<String, InputStream> opened = new LinkedHashMap<>(pluginsCache.size());
        final String location = cleanPath(uri.replaceFirst(MULTI_PREFIX, "")).trim();
        for (String url : pluginsCache.keySet()) {
            if (Wildcard.match(url, location)) {
                final StringWriter writer = new StringWriter();
                IOUtils.copy(new InputStreamReader(getInputStream(pluginsCache.get(url), url)), writer);
                final String replacedRes = processResource(writer.toString(), pluginsCache.get(url), "");
                opened.put(url, IOUtils.toInputStream(replacedRes));
            }
        }
        return new SequenceInputStream(Collections.enumeration(opened.values()));
    }

    protected InputStream getInputStream(Plugin plugin, String path) throws Exception {
        return pluginsEngine.getLoader().getResourceAsStream(plugin.getContext().getSource(), path);
    }

    protected String processResource(String text, Plugin plugin, String relPath) {
        return replaceCss(text, pluginContextPath + plugin.getId() + relPath);
    }

    @Override
    public boolean accept(String url) {
        return url.trim().startsWith(MULTI_PREFIX);
    }

    protected void fillResources() {
        final List<Plugin> pluginsList = new ArrayList<>(pluginsEngine.getPluginsMap().values());
        Collections.sort(pluginsList, new Comparator<Plugin>() {
            @Override
            public int compare(Plugin p1, Plugin p2) {
                return p1.getId().compareTo(p2.getId());
            }
        });
        for (Plugin plugin : pluginsList) {
            if (plugin != null) {
                final String cssUri = plugin.getContext().getCssPath();
                if (cssUri != null) {
                    pluginsCache.put(cssUri, plugin);
                }
                for (String jsUri : plugin.getContext().getJsPaths()) {
                    pluginsCache.put(jsUri, plugin);
                }
            }
        }
    }
}
