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
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class TestContextInjector<P> extends PluginContextInjectorImpl<P> implements PluginContextInjector<P> {
    private final Map<Class, Class> overriddenComponents = new HashMap<>();
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

    private void injectComponents(P pluginObj, final PluginContext context, final Exchange exchange){
        original.inject(pluginObj, context, exchange);
        injectOverriddenComponents(pluginObj, context, exchange);
    }

    private void injectOverriddenComponents(final Object pluginObj, final PluginContext context, final Exchange exchange) {
        try {
            final Map<Class, Object> components = new HashMap<>();
            injectField(pluginObj.getClass(), PluginComponent.class, pluginObj, new FieldListener<Object>() {
                @Override
                public Object found(Field field, AnnotationInfo info) throws Exception {

                    @SuppressWarnings("unchecked")
                    final Class<P> type = (Class<P>) overriddenComponents.get(field.getType());
                    if (type == null) {
                        return field.get(pluginObj);
                    }

                    if (!components.containsKey(type)) {
                        try {
                            final P instance = type.newInstance();
                            injectComponents(instance, context, exchange);
                            components.put(type, instance);
                        } catch (Exception e) {
                            LOGGER.warn(String.format("Failed to inject plugin component into field %s!",
                                    field.getName()), e);
                            return null;
                        }
                    }
                    return components.get(type);
                }
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
