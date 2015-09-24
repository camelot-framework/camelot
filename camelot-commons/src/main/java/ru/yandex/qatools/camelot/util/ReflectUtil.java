package ru.yandex.qatools.camelot.util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class allowing to scan all the classes inside the specified package and other class operations
 * User: isadykov
 * Date: 16.03.12
 * Time: 15:55
 */
public class ReflectUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectUtil.class);

    ReflectUtil() {
    }

    /**
     * Searches for all fields within class hierarchy
     *
     * @return
     */
    public static Field[] getFieldsInClassHierarchy(Class<?> clazz) {
        Field[] fields = {};
        while (clazz != null) {
            fields = ArrayUtils.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass(); //NOSONAR
        }
        return fields;
    }

    /**
     * Searches for the method within class hierarchy
     *
     * @return
     */
    public static Method getMethodFromClassHierarchy(Class<?> clazz, String methodName) throws NoSuchMethodException {
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass(); //NOSONAR
        }
        throw new NoSuchMethodException(methodName);
    }

    /**
     * Searches for all methods within class hierarchy
     *
     * @return
     */
    public static Method[] getMethodsInClassHierarchy(Class<?> clazz) {
        Method[] methods = {};
        while (clazz != null) {
            methods = ArrayUtils.addAll(methods, clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass(); //NOSONAR
        }
        return methods;
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(Class<?> clazz, T instance, String method, Class<?>[] argTypes,
                                             Object... arguments) throws ReflectiveOperationException {
        List<Class<?>> types = new ArrayList<>();
        if (argTypes == null) {
            for (Object arg : arguments) {
                types.add(arg.getClass());
            }
            argTypes = types.toArray(new Class<?>[types.size()]); //NOSONAR
        }
        Method m;
        try {
            m = clazz.getMethod(method, argTypes);
        } catch (NoSuchMethodException ignored) {
            LOGGER.trace("Ignored exception", ignored);
            m = clazz.getDeclaredMethod(method, argTypes);
        }
        m.setAccessible(true);
        return m.invoke(instance, arguments);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Class<?>[] argTypes, Object... arguments)
            throws ReflectiveOperationException {
        return invokeAnyMethod(instance.getClass(), instance, method, argTypes, arguments);
    }

    /**
     * Set private field
     */
    public static <T> void setPrivateField(T instance, String name, Object value)
            throws ReflectiveOperationException {
        Field field = (instance instanceof Class)
                ? ((Class) instance).getDeclaredField(name)
                : instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Object... args)
            throws ReflectiveOperationException {
        return invokeAnyMethod(instance, method, null, args);
    }


    /**
     * Get annotation value of annotation object via reflection
     */
    public static Object getAnnotationValue(Object aObj, String aValue) throws ReflectiveOperationException {
        return aObj.getClass().getMethod(aValue).invoke(aObj);
    }

    /**
     * Get annotation value of annotation object via reflection
     */
    public static Object getAnnotationValue(AnnotatedElement aobj, Class aClass, String aValue)
            throws ReflectiveOperationException {
        return getAnnotationValue(getAnnotation(aobj, aClass), aValue);
    }

    /**
     * Get annotation of an object via reflection
     */
    public static Object getAnnotation(AnnotatedElement aobj, Class aClass) throws ReflectiveOperationException {
        for (Object a : aobj.getAnnotations()) {
            if (isAnnotationInstance(aClass, a)) return a;
        }
        return null;
    }

    /**
     * Get annotation within hierarchy
     */
    public static <A extends Annotation> Object getAnnotationWithinHierarchy(Class<?> fsmClass, Class<A> aggregateClass)
            throws ReflectiveOperationException {
        while (fsmClass != null) {
            if (getAnnotation(fsmClass, aggregateClass) != null) {
                return getAnnotation(fsmClass, aggregateClass);
            }
            fsmClass = fsmClass.getSuperclass();//NOSONAR
        }
        return null;
    }

    private static boolean isAnnotationInstance(Class aClass, Object a) {
        if (Proxy.isProxyClass(a.getClass())) {
            for (Class aInterface : a.getClass().getInterfaces()) {
                if (aInterface.isAssignableFrom(aClass)) {
                    return true;
                }
            }
        }
        return aClass.isInstance(a);
    }

}
