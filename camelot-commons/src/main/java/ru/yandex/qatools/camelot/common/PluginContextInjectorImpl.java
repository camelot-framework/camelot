package ru.yandex.qatools.camelot.common;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.api.annotations.*;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ru.yandex.qatools.camelot.util.ReflectUtil.getAnnotationValue;
import static ru.yandex.qatools.camelot.util.ReflectUtil.getFieldsInClassHierarchy;
import static ru.yandex.qatools.camelot.util.ServiceUtil.forEachAnnotatedField;
import static ru.yandex.qatools.camelot.util.TypesUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class PluginContextInjectorImpl<P> implements PluginContextInjector<P> {
    protected final static Logger LOGGER = LoggerFactory.getLogger(PluginContextInjectorImpl.class);
    private static final Map<Class, Map<Class<? extends Annotation>,
            Collection<Pair<AnnotationInfo, Field>>>> cache = new ConcurrentHashMap<>();

    @Override
    public void inject(P pluginObj, PluginContext pluginConfig) {
        inject(pluginObj, pluginConfig, null);
    }

    @Override
    public void inject(final P object, final PluginContext context, final Exchange exchange) {
        if (!cache.containsKey(object.getClass())) {
            try {
                cacheClassInfo(object.getClass());
            } catch (Exception e) {
                LOGGER.error("Failed to cache the plugin context injection context!", e);
            }
        }
        performInjection(object, context, exchange);
    }

    private void performInjection(final Object object, final PluginContext context, final Exchange exchange) { //NOSONAR
        try {
            injectField(object.getClass(), MainInput.class, object, context.getMainInput());
            injectField(object.getClass(), Config.class, object, context.getAppConfig());
            injectPluginRelatedField(object, PluginStorage.class, new InjectHandler<Storage>() {
                @Override
                public Storage onByClass(Class pluginClass) {
                    return context.getInterop().storage(pluginClass);
                }

                @Override
                public Storage onById(String pluginId) {
                    return context.getInterop().storage(pluginId);
                }

                @Override
                public Storage onDefault() {
                    return context.getStorage();
                }
            });
            injectPluginRelatedField(object, Input.class, new InjectHandler<EventProducer>() {
                @Override
                public EventProducer onByClass(Class pluginClass) {
                    return context.getInterop().input(pluginClass);
                }

                @Override
                public EventProducer onById(String pluginId) {
                    return context.getInterop().input(pluginId);
                }

                @Override
                public EventProducer onDefault() {
                    return context.getInput();
                }
            });
            injectPluginRelatedField(object, Output.class, new InjectHandler<EventProducer>() {
                @Override
                public EventProducer onByClass(Class pluginClass) {
                    return context.getInterop().output(pluginClass);
                }

                @Override
                public EventProducer onById(String pluginId) {
                    return context.getInterop().output(pluginId);
                }

                @Override
                public EventProducer onDefault() {
                    return context.getOutput();
                }
            });
            injectPluginRelatedField(object, Repository.class, new InjectHandler<AggregatorRepository>() {
                @Override
                public AggregatorRepository onByClass(Class pluginClass) {
                    return context.getInterop().repo(pluginClass);
                }

                @Override
                public AggregatorRepository onById(String pluginId) {
                    return context.getInterop().repo(pluginId);
                }

                @Override
                public AggregatorRepository onDefault() {
                    return context.getRepository();
                }
            });


            injectField(object.getClass(), ClientSender.class, object, new FieldListener<ClientMessageSender>() {
                @Override
                public ClientMessageSender found(Field field, AnnotationInfo info) throws Exception { //NOSONAR
                    return context.getClientSendersProvider().getSender(info.topic);
                }
            });
            injectField(object.getClass(), Plugins.class, object, new FieldListener<PluginsInterop>() {
                @Override
                public PluginsInterop found(Field field, AnnotationInfo info) throws Exception { //NOSONAR
                    return context.getInterop();
                }
            });
            injectPluginRelatedField(object, Plugin.class, new InjectHandler<PluginInterop>() {
                @Override
                public PluginInterop onByClass(Class pluginClass) {
                    return context.getInterop().forPlugin(pluginClass);
                }

                @Override
                public PluginInterop onById(String pluginId) {
                    return context.getInterop().forPlugin(pluginId);
                }

                @Override
                public PluginInterop onDefault() {
                    return null;
                }
            });
            injectField(object.getClass(), ConfigValue.class, object, new FieldListener<Object>() {
                @Override
                public Object found(Field field, AnnotationInfo annotation) throws Exception { //NOSONAR
                    final String key = (String) annotation.value;
                    final AppConfig appConfig = context.getAppConfig();
                    Object result = appConfig.getProperty(key);
                    try { //NOSONAR
                        field.setAccessible(true);
                        if (result != null) {
                            if (isInt(field.getType())) { //NOSONAR
                                result = appConfig.getInt(key);
                            } else if (isLong(field.getType())) {
                                result = appConfig.getLong(key);
                            } else if (isDouble(field.getType())) {
                                result = appConfig.getDouble(key);
                            } else if (isBoolean(field.getType())) {
                                result = appConfig.getBoolean(key);
                            }
                        } else {
                            LOGGER.debug(String.format("Property %s is not set within current context!", key));
                        }
                    } catch (Exception ignored) {
                        LOGGER.warn("Failed to inject config value into plugin", ignored);
                    }
                    return (result != null) ? result : field.get(object);
                }
            });


            final Map<Class, Object> components = new HashMap<>();
            injectField(object.getClass(), PluginComponent.class, object, new FieldListener<Object>() {
                @Override
                public Object found(Field field, AnnotationInfo info) throws Exception { //NOSONAR
                    final Class<?> type = field.getType();
                    try { //NOSONAR
                        if (!components.containsKey(type)) {
                            final Class defaultClass = info.impl;
                            final Object instance = (defaultClass == Object.class ? type : defaultClass).newInstance();
                            performInjection(instance, context, exchange);
                            components.put(type, instance);
                        }
                    } catch (Exception e) {
                        LOGGER.warn(String.format("Failed to inject plugin component into field %s!",
                                field.getName()), e);
                    }
                    return components.get(type);
                }
            });

            if (exchange != null) {
                injectField(object.getClass(), InjectHeader.class, object, new FieldListener<Object>() {
                    @Override
                    public Object found(Field field, AnnotationInfo annotation) throws Exception { //NOSONAR
                        return exchange.getIn().getHeader((String) annotation.value);
                    }
                });
                injectField(object.getClass(), InjectHeaders.class, object, new FieldListener<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> found(Field field, AnnotationInfo annotation) throws Exception { //NOSONAR
                        return exchange.getIn().getHeaders();
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error("Inject input / output / storage of FSM " + object + " error: ", e);
        }

    }

    @SuppressWarnings("unchecked")
    protected <T extends Annotation>
    Collection<Pair<AnnotationInfo, Field>> getFields(final Class clazz,
                                                      final Class<T> annClass) throws Exception { //NOSONAR
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, new ConcurrentHashMap<Class<? extends Annotation>, Collection<Pair<AnnotationInfo, Field>>>());
        }
        if (!cache.get(clazz).containsKey(annClass)) {
            cache.get(clazz).put(annClass, new ArrayList<Pair<AnnotationInfo, Field>>());
            forEachAnnotatedField(clazz, annClass, new AnnotatedFieldListener<Object, T>() {
                @Override
                public Object found(Field field, T annotation) throws Exception { //NOSONAR
                    return cache.get(clazz).get(annClass).add(new ImmutablePair<>(new AnnotationInfo(annotation), field));
                }
            });
        }
        return cache.get(clazz).get(annClass);
    }

    @SuppressWarnings("unchecked")
    protected synchronized void cacheClassInfo(Class clazz) throws Exception { //NOSONAR
        for (Field field : getFieldsInClassHierarchy(clazz)) {
            for (Annotation annotation : field.getAnnotations()) {
                getFields(clazz, annotation.annotationType());
            }
        }
    }

    protected <T, U extends Annotation> void
    injectPluginRelatedField(Object object, final Class<U> ann,
                             final InjectHandler<T> handler) throws Exception { //NOSONAR
        injectField(object.getClass(), ann, object, new FieldListener() {
            @Override
            public Object found(Field field, AnnotationInfo info) throws Exception { //NOSONAR
                try {
                    if (info.value != null && info.value instanceof Class && info.value != Object.class) {
                        return handler.onByClass((Class) info.value);
                    }
                    if (!isEmpty(info.id)) {
                        return handler.onById(info.id);
                    }
                } catch (Exception ignored) {
                    LOGGER.warn("Failed to inject value value into plugin", ignored);
                }
                return handler.onDefault();
            }
        });
    }

    protected <T, A extends Annotation> void injectField(Class clazz, Class<A> annClass,
                                                         final Object instance, final T value) throws Exception { //NOSONAR
        injectField(clazz, annClass, instance, new FieldListener<T>() {
            @Override
            public T found(Field field, AnnotationInfo info) throws Exception {
                return value;
            }
        });
    }

    protected <A extends Annotation> void injectField(Class clazz, Class<A> annClass, final Object instance,
                                                      final FieldListener listener) throws Exception { //NOSONAR
        eachField(clazz, annClass, new FieldListener() {
            @Override
            public Object found(Field field, AnnotationInfo info) throws Exception { //NOSONAR
                Object value = listener.found(field, info);
                if (instance != null && value != null && isAssignableFrom(field.getType(), value.getClass())) {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
                return value;
            }
        });
    }

    protected <A extends Annotation> void eachField(Class clazz, Class<A> annClass,
                                                    FieldListener listener) throws Exception { //NOSONAR
        for (Pair<AnnotationInfo, Field> pair : new ArrayList<>(getFields(clazz, annClass))) {
            listener.found(pair.getValue(), pair.getKey());
        }
    }

    protected static interface InjectHandler<T> {
        T onByClass(Class pluginClass);

        T onById(String pluginId);

        T onDefault();
    }

    protected static interface FieldListener<T> {
        public T found(Field field, AnnotationInfo info) throws Exception; //NOSONAR
    }

    protected static class AnnotationInfo {
        String id;
        Object value;
        String topic;
        Class impl;

        public <T extends Annotation> AnnotationInfo(T annotation) {
            try {
                value = getAnnotationValue(annotation, "value");
            } catch (Exception e) {
                LOGGER.trace("Failed to get annotation field `value` for " + annotation, e);
            }
            try {
                id = (String) getAnnotationValue(annotation, "id");
            } catch (Exception e) {
                LOGGER.trace("Failed to get annotation field `id` for " + annotation, e);
            }
            try {
                topic = (String) getAnnotationValue(annotation, "topic");
            } catch (Exception e) {
                LOGGER.trace("Failed to get annotation field `topic` for " + annotation, e);
            }
            try {
                impl = (Class) getAnnotationValue(annotation, "impl");
            } catch (Exception e) {
                LOGGER.trace("Failed to get annotation field `impl` for " + annotation, e);
            }
        }
    }
}
