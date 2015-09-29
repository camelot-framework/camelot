package ru.yandex.qatools.camelot.common;

import ru.yandex.qatools.camelot.api.annotations.AggregationKey;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.api.annotations.Split;
import ru.yandex.qatools.camelot.error.MetadataException;
import ru.yandex.qatools.fsm.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.collectAllSuperclassesAndInterfaces;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.getMethodsInClassHierarchy;

/**
 * @author Ilya Sadykov
 */
public abstract class Metadata {

    Metadata() {
    }

    private static final Map<Class<?>, MetadataClassInfo> cache = new ConcurrentHashMap<>();

    public static <T> MetadataClassInfo getMeta(Class<T> clazz, Class<? extends MetadataClassInfo> metaClass) {
        if (!cache.containsKey(clazz)) {
            try {
                Constructor<? extends MetadataClassInfo> c = metaClass.getConstructor(Class.class);
                cache.put(clazz, c.newInstance(clazz));
            } catch (Exception e) {
                throw new MetadataException("Failed to instantiate the metadata reader for class " + metaClass, e);
            }
        }
        return cache.get(clazz);
    }

    public static <T> MetadataClassInfo getMeta(Class<T> clazz) {
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, new ClassInfo<>(clazz));
        }
        return cache.get(clazz);
    }

    @ScanMethodsAnnotatedWith({
            OnException.class, OnTransit.class, BeforeTransit.class,
            AfterTransit.class, Processor.class, AggregationKey.class, Split.class, NewState.class
    })
    public static class ClassInfo<T> implements MetadataClassInfo {
        private final Class<T> clazz;
        private final Map<Class<? extends Annotation>, Method[]> annotatedMethods = new HashMap<>();
        private final Map<Class<? extends Annotation>, Map<Set<Class>, Set<Method>>> paramTypesMethods = new HashMap<>();
        private final Map<Class, Class[]> superClassesCache = new HashMap<>();
        private final Class<? extends Annotation>[] methodAnnotations = getMethodAnnotations();

        public ClassInfo(Class<T> clazz) {
            this.clazz = clazz;
            buildMethodsCache();
            collectStateSuperClassesCache();
        }

        @Override
        public Method[] getAnnotatedMethods(Class aClass) {
            if (annotatedMethods.containsKey(aClass)) {
                return annotatedMethods.get(aClass);
            }
            return new Method[]{};
        }

        @Override
        public Class[] getSuperClasses(Class clazz) {
            if (superClassesCache.containsKey(clazz)) {
                return superClassesCache.get(clazz);
            }
            final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
            final Class[] superClasses = classes.toArray(new Class[classes.size()]);
            addCollectedSuperclasses(superClassesCache, superClasses);
            return superClasses;
        }

        @Override
        public Collection<Method> getMethodsByParamTypes(Class<? extends Annotation> aClass, Class... paramType) {
            Set<Method> result = new HashSet<>();
            if (paramTypesMethods.containsKey(aClass)) {
                Set<Class> key = new HashSet<>();
                key.addAll(asList(paramType));
                if (paramTypesMethods.get(aClass).containsKey(key)) {
                    result.addAll(paramTypesMethods.get(aClass).get(key));
                }
            }
            return result;
        }

        private void collectStateSuperClassesCache() {
            for (Class<? extends Annotation> annClass : methodAnnotations) {
                for (Method method : getAnnotatedMethods(annClass)) {
                    for (Class<?> paramClass : method.getParameterTypes()) {
                        addCollectedSuperclasses(superClassesCache, paramClass);
                    }
                }
            }
        }

        private static void addCollectedSuperclasses(Map<Class, Class[]> superclasses, Class... eventClass) {
            for (Class clazz : eventClass) {
                if (!superclasses.containsKey(clazz)) {
                    final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
                    final Class[] classesArray = classes.toArray(new Class[classes.size()]);
                    superclasses.put(clazz, classesArray);
                    addCollectedSuperclasses(superclasses, classesArray);
                }
            }
        }

        private void buildMethodsCache() {
            for (Class<? extends Annotation> annClass : methodAnnotations) {
                List<Method> methods = new ArrayList<>();
                for (Method method : getMethodsInClassHierarchy(clazz)) {
                    if (method.getAnnotation(annClass) != null) {
                        methods.add(method);
                        buildMethodParamsInfoCache(annClass, method);
                    }
                }
                annotatedMethods.put(annClass, methods.toArray(new Method[methods.size()]));
            }
        }

        private void buildMethodParamsInfoCache(Class<? extends Annotation> annClass, Method method) {
            Set<Class> paramTypes = new HashSet<>();
            paramTypes.addAll(asList(method.getParameterTypes()));
            if (!paramTypesMethods.containsKey(annClass)) {
                paramTypesMethods.put(annClass, new HashMap<Set<Class>, Set<Method>>());
            }
            if (!paramTypesMethods.get(annClass).containsKey(paramTypes)) {
                paramTypesMethods.get(annClass).put(paramTypes, new HashSet<Method>());
            }
            paramTypesMethods.get(annClass).get(paramTypes).add(method);
        }

        protected Class<? extends Annotation>[] getMethodAnnotations() {
            ScanMethodsAnnotatedWith config = getClass().getAnnotation(ScanMethodsAnnotatedWith.class);
            return config.value();
        }

    }
}
