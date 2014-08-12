package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.Exchange;
import ru.yandex.qatools.camelot.api.annotations.PluginComponent;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.PluginContextInjector;
import ru.yandex.qatools.camelot.core.impl.PluginContextInjectorImpl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestContextInjector<P> extends PluginContextInjectorImpl<P> implements PluginContextInjector<P> {
    private final Map<Class, Class> overridenComponents = new HashMap<>();
    private final PluginContextInjector<P> original;

    public TestContextInjector(PluginContextInjector<P> original) {
        this.original = original;
    }

    @Override
    public void inject(P pluginObj, PluginContext context) {
        injectComponents(pluginObj, context, null);
    }

    @Override
    public void inject(P pluginObj, PluginContext context, Exchange exchange) {
        injectComponents(pluginObj, context, exchange);
    }

    private void injectComponents(Object pluginObj, final PluginContext context, final Exchange exchange){
        original.inject((P) pluginObj, context, exchange);
        injectOverridenComponents(pluginObj, context, exchange);
    }

    private void injectOverridenComponents(Object pluginObj, final PluginContext context, final Exchange exchange) {
        try {
            final Map<Class, Object> components = new HashMap<>();
            injectField(pluginObj.getClass(), PluginComponent.class, pluginObj, new FieldListener<Object>() {
                @Override
                public Object found(Field field, AnnotationInfo info) throws Exception {
                    final Class<?> type = (overridenComponents.containsKey(field.getType())) ?
                            overridenComponents.get(field.getType()) : field.getType();
                    try {
                        if (!components.containsKey(type)) {
                            final Object instance = type.newInstance();
                            injectComponents(instance, context, exchange);
                            components.put(type, instance);
                        }
                    } catch (Exception e) {
                        LOGGER.warn(String.format("Failed to inject plugin component into field %s!", field.getName()), e);
                    }
                    return components.get(type);
                }
            });
        } catch (Exception e) {
            LOGGER.warn(String.format("Failed to inject context of plugin %s!", context.getId()), e);
        }
    }

    public void reset() {
        overridenComponents.clear();
    }

    public void overrideComponent(Class from, Class to) {
        overridenComponents.put(from, to);
    }

}
