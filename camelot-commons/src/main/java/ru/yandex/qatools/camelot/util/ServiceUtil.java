package ru.yandex.qatools.camelot.util;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.common.AnnotatedFieldListener;
import ru.yandex.qatools.camelot.common.AnnotatedMethodListener;
import ru.yandex.qatools.camelot.common.BasicEventProducer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.Thread.currentThread;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.TypesUtil.isAssignableFrom;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class ServiceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUtil.class);

    ServiceUtil() {
    }

    /**
     * Inject a lazy value to the annotated field if found
     */
    public static <T, U extends Annotation> void injectAnnotatedField(
            Class clazz,
            final Object instance,
            Class<U> annotation,
            final AnnotatedFieldListener<T, U> value) throws Exception { //NOSONAR
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
            throws Exception { //NOSONAR
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
    ) throws Exception { //NOSONAR
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
    ) throws Exception { //NOSONAR
        injectAnnotatedField(clazz, instance, annotation, new AnnotatedFieldListener<T, U>() {
            @Override
            public T found(Field field, U annotation) {
                return value;
            }
        });
    }

    /**
     * Remove all the endpoints associated with uri
     */
    public static void gracefullyRemoveEndpoints(CamelContext camelContext, String uri) throws Exception { //NOSONAR
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
    public static void gracefullyRemoveRoute(CamelContext camelContext, String id) throws Exception { //NOSONAR
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
    public static void gracefullyStartRoute(CamelContext camelContext, String id) throws Exception { //NOSONAR
        if (camelContext.getRoute(id) != null) {
            LOGGER.info("Gracefully starting route " + id);
            try {
                camelContext.startRoute(id);
            } catch (Exception e) {
                LOGGER.error("Failed to start route: " + id, e);
            }
        }
    }

    /**
     * Search for the annotated field within the procClass
     * and initialize the Camel producer for the uri
     */
    public static EventProducer initEventProducer(CamelContext camelContext, final String uri, final MessagesSerializer serializer)
            throws Exception { //NOSONAR
        final ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.setDefaultEndpointUri(uri);
        return new BasicEventProducer(producerTemplate) {
            @Override
            public void produce(Object event, Map<String, Object> headers) {
                try {
                    final ClassLoader eventCL = event.getClass().getClassLoader();
                    final ClassLoader contextCL = currentThread().getContextClassLoader();
                    final ClassLoader cl = (eventCL != null) ? eventCL : ((contextCL != null) ? contextCL : getSystemClassLoader());
                    producerTemplate.sendBodyAndHeaders(
                            serializer.processBodyAndHeadersBeforeSend(event, headers, cl), headers);
                } catch (Exception e) {
                    LOGGER.error("Failed to produce message to the uri {}: {}", uri, formatStackTrace(e)); //NOSONAR
                }
            }
        };
    }
}
