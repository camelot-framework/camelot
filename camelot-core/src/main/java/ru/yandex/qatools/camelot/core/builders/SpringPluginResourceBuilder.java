package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import ru.yandex.qatools.camelot.api.annotations.Repository;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.error.PluginsSystemException;
import ru.yandex.qatools.camelot.spring.ClassLoaderBeanDefinition;

import javax.ws.rs.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.currentThread;
import static java.util.Collections.sort;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.beans.factory.config.AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
import static ru.yandex.qatools.camelot.util.NameUtil.pluginResourceBeanName;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.ServiceUtil.injectAnnotatedField;
import static ru.yandex.qatools.camelot.util.ServiceUtil.injectTmpInputBufferProducers;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SpringPluginResourceBuilder implements ResourceBuilder, BeanFactoryAware {
    final Logger logger = LoggerFactory.getLogger(getClass());
    BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    private static final String[] TPL_EXTS = new String[]{".jade", ".mustache", ".html"};
    private static final String[] CSS_EXTS = new String[]{".css", ".less"};
    private static final String[] JS_EXTS = new String[]{".js", ".coffee"};

    /**
     * Build resources and add them to the context
     */
    @Override
    public void build(CamelContext camelContext, Plugin plugin) throws Exception {
        final String pluginId = plugin.getId();
        final ClassLoader classLoader = plugin.getContext().getClassLoader();
        if (!isBlank(plugin.getResource())) {
            final Class resClass = classLoader.loadClass(plugin.getResource());
            registerResBean(resClass, pluginResourceBeanName(pluginId), plugin, classLoader, camelContext);
            plugin.getContext().setResPathMapping(getPathMapping(resClass));
            initResourcesConfig(resClass, plugin);
        }
        if (isBlank(plugin.getContext().getResDirPath())) {
            initResourcesConfig(classLoader.loadClass(plugin.getContext().getPluginClass()), plugin);
        }
    }

    /**
     * Remove the resources from the context
     */
    @Override
    public void remove(CamelContext camelContext, Plugin plugin) throws Exception {
        final String pluginId = plugin.getId();
        final ClassLoader classLoader = plugin.getContext().getClassLoader();
        if (plugin.getResource() != null) {
            final Class resClass = classLoader.loadClass(plugin.getResource());
            final String beanName = pluginResourceBeanName(pluginId);
            injectTmpInputBufferProducers(plugin.getContext().getEndpoints(), beanFactory.getBean(beanName), resClass,
                    camelContext, plugin.getContext().getTmpBufferUri());
        }
    }

    private void initResourcesConfig(Class baseClass, Plugin plugin) throws Exception {
        plugin.getContext().setCssPath(findTemplatePath(baseClass, "**/*", CSS_EXTS));
        plugin.getContext().getJsPaths().addAll(findTemplatePaths(baseClass, "**/*", JS_EXTS));
        plugin.getContext().setDashboardPath(findTemplatePath(baseClass, "dashboard", TPL_EXTS));
        plugin.getContext().setWidgetPath(findTemplatePath(baseClass, "widget", TPL_EXTS));
        plugin.getContext().setResDirPath(findTemplatePath(baseClass, "", ""));
    }

    private List<String> findTemplatePaths(Class baseClass, String fileBaseName, String... extensions) {
        List<String> paths = new ArrayList<>();
        final String packageName = baseClass.getPackage().getName() + "." + baseClass.getSimpleName();
        final String basePath = packageName.replaceAll("\\.", File.separator) + File.separator;
        for (String ext : extensions) {
            try {
                for (String res : resolveResourcesAsStringsFromPattern("classpath:" + basePath + fileBaseName + ext, baseClass)) {
                    paths.add(res.substring(res.indexOf(basePath)));
                }
            } catch (Exception e) {
                logger.trace("Failed to find template paths", e);
            }
        }
        sort(paths);
        return paths;
    }

    private String findTemplatePath(Class resClass, String fileBaseName, String... extensions) {
        final List<String> paths = findTemplatePaths(resClass, fileBaseName, extensions);
        if (paths.size() > 0) {
            return paths.get(0);
        }
        return null;
    }

    private Object registerResBean(Class clazz, String beanName, Plugin plugin, ClassLoader classLoader, CamelContext camelContext) throws Exception {
        ClassLoader contextLoader = currentThread().getContextClassLoader();
        currentThread().setContextClassLoader(classLoader);
        try {
            if (!(beanFactory instanceof ConfigurableListableBeanFactory) ||
                    !(beanFactory instanceof BeanDefinitionRegistry)) {
                throw new PluginsSystemException("Failed to initialize the resources beans: beanFactory must be " +
                        "instance of BeanDefinitionRegistry and ConfigurableListableBeanFactory");
            }

            final ConfigurableListableBeanFactory defaultBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
            final BeanDefinitionRegistry beanReg = (BeanDefinitionRegistry) beanFactory;

            // replace old definitions with the new ones
            if (defaultBeanFactory.containsBeanDefinition(beanName)) {
                ((BeanDefinitionRegistry) defaultBeanFactory).removeBeanDefinition(beanName);
            }

            beanReg.registerBeanDefinition(beanName, new ClassLoaderBeanDefinition(clazz, classLoader));

            final Object res = defaultBeanFactory.createBean(clazz, AUTOWIRE_BY_NAME, true);
            defaultBeanFactory.autowireBean(res);
            DefaultCamelBeanPostProcessor processor = new DefaultCamelBeanPostProcessor(camelContext);
            processor.postProcessBeforeInitialization(res, null);
            plugin.getContext().getInjector().inject(res, plugin.getContext(), null);
            injectAnnotatedField(clazz, res, Repository.class, plugin.getContext().getRepository());
            defaultBeanFactory.registerSingleton(beanName, res);
            return res;
        } finally {
            currentThread().setContextClassLoader(contextLoader);
        }
    }

    /**
     * Returns the path mapping for the class
     */
    private String getPathMapping(Class resClass) throws Exception {
        Object path = getAnnotation(resClass, Path.class);
        return (path != null) ? (String) getAnnotationValue(path, "value") : null;
    }
}
