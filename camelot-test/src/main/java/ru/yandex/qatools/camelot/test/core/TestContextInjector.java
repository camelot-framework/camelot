package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.api.annotations.PluginComponent;
import ru.yandex.qatools.camelot.common.PluginContextInjector;
import ru.yandex.qatools.camelot.common.PluginContextInjectorImpl;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class TestContextInjector<P> extends PluginContextInjectorImpl<P> implements PluginContextInjector<P> {
    private final Map<Class, Class> overriddenComponents = new HashMap<>();
    private final PluginContextInjector<P> original;

    public TestContextInjector(PluginContextInjector<P> original) {
        this.original = original;
    }

    @Override
    public void inject(P pluginObj, PluginsService service, PluginContext context) {
        injectComponents(pluginObj, service, context, null);
    }

    @Override
    public void inject(P pluginObj, PluginsService service, PluginContext context, Exchange exchange) {
        injectComponents(pluginObj, service, context, exchange);
    }

    private void injectComponents(P pluginObj, PluginsService service, PluginContext context, Exchange exchange) {
        original.inject(pluginObj, service, context, exchange);
        injectOverriddenComponents(pluginObj, service, context, exchange);
    }

    private void injectOverriddenComponents(Object pluginObj, PluginsService service,
                                            PluginContext context, Exchange exchange) {
        try {
            final Map<Class, Object> components = new HashMap<>();
            injectField(pluginObj.getClass(), PluginComponent.class, pluginObj, (field, info) -> { //NOSONAR

                @SuppressWarnings("unchecked")
                final Class<P> type = (Class<P>) overriddenComponents.get(field.getType());
                if (type == null) {
                    return field.get(pluginObj);
                }

                if (!components.containsKey(type)) {
                    try {
                        final P instance = type.newInstance();
                        injectComponents(instance, service, context, exchange);
                        components.put(type, instance);
                    } catch (Exception e) {
                        LOGGER.warn(String.format("Failed to inject plugin component into field %s!",
                                field.getName()), e);
                        return null;
                    }
                }
                return components.get(type);
            });
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to inject context of plugin %s!", context.getId()), e);
        }
    }

    public void reset() {
        overriddenComponents.clear();
    }

    public void overrideComponent(Class from, Class to) {
        overriddenComponents.put(from, to);
    }

}
