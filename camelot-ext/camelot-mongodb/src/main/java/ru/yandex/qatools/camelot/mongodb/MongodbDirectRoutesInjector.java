package ru.yandex.qatools.camelot.mongodb;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import ru.yandex.qatools.camelot.common.PluginUriBuilder;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.Plugin;

import static java.lang.reflect.Proxy.newProxyInstance;
import static ru.yandex.qatools.camelot.mongodb.MongodbDirectRoutesInitializer.URI_PREFIX;
import static ru.yandex.qatools.camelot.mongodb.MongodbDirectRoutesInitializer.overridenUri;

/**
 * @author Ilya Sadykov
 */
public class MongodbDirectRoutesInjector implements BeanPostProcessor, PriorityOrdered {
    private static void overrideUriBuilder(PluginsService pluginsService) {
        final PluginUriBuilder original = pluginsService.getUriBuilder();
        pluginsService.setUriBuilder(
                (PluginUriBuilder) newProxyInstance(pluginsService.getClass().getClassLoader(),
                        new Class[]{PluginUriBuilder.class}, (proxy, method, args) -> {
                            if ("pluginUri".equals(method.getName())) {
                                final Plugin plugin = (Plugin) args[0];
                                if (plugin.getBaseInputUri().equals(URI_PREFIX)) {
                                    return overridenUri(plugin, (String) args[1]);
                                }
                            }
                            return method.invoke(original, args);
                        }));
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof PluginsService) {
            overrideUriBuilder((PluginsService) bean);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1000;
    }
}
