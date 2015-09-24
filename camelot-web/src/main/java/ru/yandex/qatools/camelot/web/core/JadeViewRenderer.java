package ru.yandex.qatools.camelot.web.core;

import de.neuland.jade4j.Jade4J;
import de.neuland.jade4j.parser.Parser;
import de.neuland.jade4j.parser.node.Node;
import de.neuland.jade4j.template.JadeTemplate;
import de.neuland.jade4j.template.ReaderTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import ru.yandex.qatools.camelot.web.ViewRenderer;

import javax.servlet.ServletContext;
import javax.ws.rs.NotFoundException;
import java.io.*;
import java.util.Map;

import static java.io.File.separator;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Component
public class JadeViewRenderer implements ViewRenderer, ApplicationContextAware {
    private static final String DEFAULT_LAYOUT = "main.jade";
    private static final Logger logger = LoggerFactory.getLogger(JadeViewRenderer.class);
    public static final String ERR_500_TPL = "500.jade";
    public static final String ERR_400_TPL = "404.jade";

    @Autowired
    ViewHelper viewHelper;

    private ServletContext servletContext;

    private final Map<String, Object> defaultMap;

    public final String layoutsPath;
    public final String viewsPath;
    public final String errorsPath;

    @Autowired
    public JadeViewRenderer(@Value("${jade.layouts.path}") String layoutsPath,
                            @Value("${jade.views.path}") String viewsPath,
                            @Value("${jade.errors.path}") String errorsPath,
                            ViewHelper viewHelper) {
        this.layoutsPath = layoutsPath;
        this.viewsPath = viewsPath;
        this.errorsPath = errorsPath;
        this.defaultMap = map("view", (Object) viewHelper);
    }

    @Override
    public String render(String viewFileName) throws IOException {
        return render(null, viewFileName, defaultMap);
    }

    @Override
    public String render(String viewFileName, Map<String, Object> attrs) throws IOException {
        return render(null, viewFileName, attrs);
    }

    @Override
    public String renderWithDefaultLayout(String viewFileName) throws IOException {
        return renderWithDefaultLayout(viewFileName, defaultMap);
    }

    @Override
    public <T> String renderWithDefaultLayout(Class<T> source) throws IOException {
        return renderWithDefaultLayout(source, defaultMap);
    }

    @Override
    public String renderWithDefaultLayout(String viewFileName, Map<String, Object> attrs) throws IOException {
        return render(DEFAULT_LAYOUT, viewFileName, attrs);
    }

    @Override
    public String renderWithDefaultLayout(Class source, Map<String, Object> attrs) throws IOException {
        String viewFileName = source.getSimpleName().replace(".", separator) + ".index.jade";
        return render(DEFAULT_LAYOUT, viewFileName, attrs);
    }

    @Override
    public String render(String layoutFileName, InputStream layout, String viewFileName, InputStream template) throws IOException {
        return render(layoutFileName, layout, viewFileName, template, defaultMap);
    }

    @Override
    public String render(final String layoutName, final String viewFileName) throws IOException {
        return render(
                layoutName,
                layoutName != null ? getResStream(layoutsPath, layoutName) : null,
                viewFileName,
                getResStream(viewsPath, layoutName)
        );
    }

    @Override
    public String render(String layoutName, String viewName, Map<String, Object> attrs) throws IOException {
        return render(
                layoutName,
                layoutName != null ? getResStream(layoutsPath, layoutName) : null,
                viewName,
                getResStream(viewsPath, viewName),
                attrs
        );
    }

    @Override
    public String render(final String layoutFileName, final InputStream layout, final String viewFileName,
                         final InputStream view, Map<String, Object> attrs) throws IOException {
        try {
            if (view == null) {
                return renderError(ERR_400_TPL, new NotFoundException("View " + viewFileName + " not found!"));
            }
            String viewHtml = renderJadeView(viewFileName, view, appendAttrs(attrs));
            if (layout != null && layoutFileName != null) {
                attrs.put("yield", viewHtml);
                return renderJadeView(layoutFileName, layout, appendAttrs(attrs));
            }
            return viewHtml;
        } catch (Exception e) {
            return renderError(ERR_500_TPL, e);
        }
    }

    public String renderError(String viewPath, Exception e) throws IOException {
        final String view = renderJadeView(viewPath, getResStream(errorsPath, viewPath),
                appendAttrs(map("error", (Object) e)));
        return renderJadeView(DEFAULT_LAYOUT, getResStream(layoutsPath, DEFAULT_LAYOUT),
                appendAttrs(map("yield", (Object) view)));
    }

    protected Map<String, Object> appendAttrs(Map<String, Object> attrs) {
        attrs.put("view", this);
        if (servletContext != null) {
            attrs.put("context_path", servletContext.getContextPath());
        }
        return attrs;
    }

    protected InputStream getResStream(String dir, String file) throws IOException {
        final String path = dir + File.separator + file;
        if (servletContext != null) {
            return servletContext.getResourceAsStream(path);
        }
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    protected String renderJadeView(String fileName, final InputStream stream, final Map<String, Object> attrs) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final ReaderTemplateLoader loader = new ReaderTemplateLoader(reader, fileName);
            Parser parser = new Parser(fileName, loader);
            Node root = parser.parse();
            JadeTemplate template = new JadeTemplate();
            template.setTemplateLoader(loader);
            template.setRootNode(root);
            return Jade4J.render(template, attrs);
        } catch (Exception e) {
            logger.error("Failed to render view " + fileName, e);
            if (!fileName.contains(ERR_500_TPL)) {
                return renderError(ERR_500_TPL, e);
            }
            return "500 Unexpected server error: " + e.getMessage();
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            servletContext = applicationContext.getBean(ServletContext.class);
        } catch (Exception e) {
            logger.warn("Could not find the ServletContext bean within the application context!", e);
        }
    }
}
