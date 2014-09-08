package ru.yandex.qatools.camelot.util;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.api.annotations.Input;
import ru.yandex.qatools.camelot.api.annotations.MainInput;
import ru.yandex.qatools.camelot.api.annotations.Output;
import ru.yandex.qatools.camelot.beans.IncomingMessage;
import ru.yandex.qatools.camelot.core.AnnotatedFieldListener;
import ru.yandex.qatools.camelot.core.AnnotatedMethodListener;
import ru.yandex.qatools.camelot.core.impl.AppConfigSystemProperties;
import ru.yandex.qatools.camelot.core.impl.BasicEventProducer;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;
import static ru.yandex.qatools.camelot.util.IOUtils.readResource;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;
import static ru.yandex.qatools.camelot.util.TypesUtil.isAssignableFrom;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ServiceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);

    /**
     * Search for the annotated field within the procClass
     * and initialize the Camel producer for the uri
     */
    public static EventProducer initEventProducer(CamelContext camelContext, final String uri)
            throws Exception {
        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.setDefaultEndpointUri(uri);
        return new BasicEventProducer(producerTemplate) {
            @Override
            public void produce(Object event, Map<String, Object> headers) {
                try {
                    headers.put(BODY_CLASS, event.getClass().getName());
                    producerTemplate.sendBodyAndHeaders(serializeToBytes(event), headers);
                } catch (Exception e) {
                    logger.error("Failed to produce message to the uri " + uri, e);
                }
            }
        };

    }

    /**
     * Initializes the client sender to the topic with uri
     */
    public static ClientMessageSender initEventSender(
            CamelContext camelContext, String uri, final String topic) throws Exception {
        final EventProducer producer = initEventProducer(camelContext, uri);
        return new ClientMessageSender() {
            @Override
            public void send(Object message) {
                send(topic, message);
            }

            @Override
            public void send(String topic, Object message) {
                producer.produce(message, TOPIC, topic);
            }
        };
    }

    /**
     * Initialize the plugin source app configuration
     */
    public static AppConfig initPluginAppConfig(
            AppConfig baseConfig, ClassLoader loader, String path)
            throws IOException, ClassNotFoundException, URISyntaxException {
        final Properties properties = new Properties();
        for (Resource resource : resolveResourcesFromPattern(path, loader)) {
            properties.load(readResource(resource.getURL()));
        }
        return wrapPluginAppConfig(baseConfig, properties);
    }

    /**
     * Wrap application config with properties
     */
    public static AppConfig wrapPluginAppConfig(final AppConfig baseConfig,
                                                final Properties properties) {
        return new AppConfigSystemProperties() {
            @Override
            public String getProperty(String key) {
                final String res = baseConfig.getProperty(key);
                return (res != null) ? res : properties.getProperty(key);
            }
        };
    }

    /**
     * Inject a lazy value to the annotated field if found
     */
    public static <T, U extends Annotation> void injectAnnotatedField(
            Class clazz,
            final Object instance,
            Class<U> annotation,
            final AnnotatedFieldListener<T, U> value) throws Exception {
        forEachAnnotatedField(clazz, annotation, new AnnotatedFieldListener<T, U>() {
            @Override
            public T found(Field field, U annValue) throws Exception {
                T res = value.found(field, annValue);
                if (instance != null && res != null
                        && isAssignableFrom(field.getType(), res.getClass())) {
                    boolean oldAccessible = field.isAccessible();
                    field.setAccessible(true);
                    field.set(instance, res);
                    field.setAccessible(oldAccessible);
                }
                return res;
            }
        });
    }

    /**
     * Sets the value for the field of the instance
     */
    public static <T> T setFieldValue(Field field, Object instance, T value)
            throws IllegalAccessException {
        if (instance != null) {
            boolean oldAccessible = field.isAccessible();
            field.setAccessible(true);
            field.set(instance, value);
            field.setAccessible(oldAccessible);
            return value;
        }
        return null;
    }

    /**
     * Perform an action on each annotated method
     */
    @SuppressWarnings("unchecked")
    public static <T, A> void forEachAnnotatedMethod(
            Class clazz, Class<A> annotation, AnnotatedMethodListener<T, A> listener)
            throws Exception {
        for (Method method : getMethodsInClassHierarchy(clazz)) {
            final A annValue = (A) getAnnotation(method, annotation);
            if (annValue != null) {
                listener.found(method, annValue);
            }
        }
    }

    /**
     * Perform an action on each annotated field
     */
    public static <T, U> void forEachAnnotatedField(
            Class clazz,
            Class<U> annotationClass,
            AnnotatedFieldListener<T, U> listener
    ) throws Exception {
        for (Field field : getFieldsInClassHierarchy(clazz)) {
            //noinspection unchecked
            final U annValue = (U) getAnnotation(field, annotationClass);
            if (annValue != null) {
                listener.found(field, annValue);
            }
        }
    }

    /**
     * Inject a value to the annotated field if found
     */
    public static <T, U extends Annotation> void injectAnnotatedField(
            Class clazz,
            Object instance,
            Class<U> annotation,
            final T value
    ) throws Exception {
        injectAnnotatedField(clazz, instance, annotation, new AnnotatedFieldListener<T, U>() {
            @Override
            public T found(Field field, U annotation) {
                return value;
            }
        });
    }

    /**
     * Inject the producers for each field within resource
     */
    public static void injectTmpInputBufferProducers(final PluginEndpoints endpoints, final Object res, final Class resClass, final CamelContext camelContext, String tmpInputBufferUri) throws Exception {
        injectTmpInputBufferProducer(res, resClass, MainInput.class, camelContext, endpoints.getMainInputUri(), tmpInputBufferUri);
        injectTmpInputBufferProducer(res, resClass, Input.class, camelContext, endpoints.getInputUri(), tmpInputBufferUri);
        injectTmpInputBufferProducer(res, resClass, Output.class, camelContext, endpoints.getOutputUri(), tmpInputBufferUri);
    }

    /**
     * Inject the producer field with uri
     */
    public static <U extends Annotation> void injectTmpInputBufferProducer(final Object res, final Class resClass, Class<U> annClass,
                                                                           final CamelContext camelContext, final String uri, final String tmpInputBufferUri) throws Exception {
        forEachAnnotatedField(resClass, annClass, new AnnotatedFieldListener<Object, U>() {
            @Override
            public Object found(Field field, Annotation annotation) throws Exception {
                return setFieldValue(field, res, initTmpInputBufferProducer(camelContext, uri, tmpInputBufferUri));
            }
        });
    }

    /**
     * Init the tmp input producer
     */
    public static EventProducer initTmpInputBufferProducer(CamelContext camelContext,
                                                           final String uri, final String bufferUri) {
        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.setDefaultEndpointUri(bufferUri);
        return new BasicEventProducer(producerTemplate) {
            @Override
            public void produce(Object event, Map<String, Object> headers) {
                try {
                    producerTemplate.sendBodyAndHeaders(
                            new IncomingMessage(uri, serializeToBytes(event)), headers
                    );
                } catch (Exception e) {
                    logger.error(format("Failed to produce message to the tmp buffer %s for uri %s ", bufferUri, uri), e);
                }
            }
        };
    }

    /**
     * Remove all the endpoints associated with uri
     */
    public static void gracefullyRemoveEndpoints(CamelContext camelContext, String uri) throws Exception {
        try {
            LOGGER.info("Gracefully removing endpoint " + uri);
            final Endpoint endpoint = camelContext.getEndpoint(uri);
            if (endpoint != null) {
                endpoint.stop();
                camelContext.removeEndpoints(endpoint.getEndpointUri());
            }
        } catch (Exception e) {
            LOGGER.debug("Failed to remove endpoint: " + uri, e);
        }
    }

    /**
     * Stop and remove the route by id
     */
    public static void gracefullyRemoveRoute(CamelContext camelContext, String id) throws Exception {
        if (camelContext.getRoute(id) != null) {
            LOGGER.info("Gracefully removing route " + id);
            try {
                camelContext.stopRoute(id, 10, TimeUnit.SECONDS);
                camelContext.removeRoute(id);
            } catch (Exception e) {
                LOGGER.debug("Failed to remove route: " + id, e);
            }
        }
    }

    /**
     * Start the route by id
     */
    public static void gracefullyStartRoute(CamelContext camelContext, String id) throws Exception {
        if (camelContext.getRoute(id) != null) {
            LOGGER.info("Gracefully starting route " + id);
            try {
                camelContext.startRoute(id);
            } catch (Exception e) {
                LOGGER.error("Failed to start route: " + id, e);
            }
        }
    }
}
