package ru.yandex.qatools.camelot.core.web.wro;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.processor.impl.css.CssUrlRewritingProcessor;

import javax.servlet.ServletContext;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginsResourceCssUrlPreProcessor extends CssUrlRewritingProcessor {
    public static final String ALIAS = "pluginsCssUrlRewriting";
    final WebApplicationContext context;
    final ServletContext servletContext;
    final String pluginContextPath;

    public PluginsResourceCssUrlPreProcessor() {
        this.servletContext = Context.get().getServletContext();
        this.context = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        pluginContextPath = servletContext.getContextPath() + "/plugin/";
    }

    @Override
    protected boolean isReplaceNeeded(String url) {
        return !url.startsWith(pluginContextPath) && super.isReplaceNeeded(url);
    }
}
