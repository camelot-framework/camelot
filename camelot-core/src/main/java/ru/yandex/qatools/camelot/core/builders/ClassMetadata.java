package ru.yandex.qatools.camelot.core.builders;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ru.yandex.qatools.fsm.utils.ReflectUtils.collectAllSuperclassesAndInterfaces;
import static ru.yandex.qatools.fsm.utils.ReflectUtils.getMethodsInClassHierarchy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ClassMetadata {

    private static final Map<Class<?>, ClassInfo> CACHE = new ConcurrentHashMap<>();

    ClassMetadata() {
    }

    public static <T> ClassInfo get(Class<T> clazz) {
        if (!CACHE.containsKey(clazz)) {
            CACHE.put(clazz, new ClassInfo(clazz));
        }
        return CACHE.get(clazz);
    }

    public static class ClassInfo {

        private final Class clazz;
        private final Map<Class<? extends Annotation>, Method[]> annotatedMethods = new HashMap<>();
        private final Map<Class, Class[]> superClassesCache = new HashMap<>();

        public ClassInfo(Class clazz) {
            this.clazz = clazz;
        }

        public <A extends Annotation> Method[] getAnnotatedMethods(Class<A> annClass) {
            if (!annotatedMethods.containsKey(annClass)) {
                List<Method> methods = new ArrayList<>();
                for (Method method : getMethodsInClassHierarchy(clazz)) {
                    if (method.getAnnotation(annClass) != null) {
                        methods.add(method);
                    }
                }
                annotatedMethods.put(annClass, methods.toArray(new Method[methods.size()]));
            }
            return annotatedMethods.get(annClass);
        }

        public Class[] getSuperClasses() {
            if (superClassesCache.containsKey(clazz)) {
                return superClassesCache.get(clazz);
            }
            final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
            final Class[] superClasses = classes.toArray(new Class[classes.size()]);
            addCollectedSuperclasses(superClassesCache, superClasses);
            return superClasses;
        }

        private void addCollectedSuperclasses(Map<Class, Class[]> superclasses, Class... stateClass) {
            for (Class clazz : stateClass) {
                if (!superclasses.containsKey(clazz)) {
                    final List<Class> classes = collectAllSuperclassesAndInterfaces(clazz);
                    final Class[] classesArray = classes.toArray(new Class[classes.size()]);
                    superclasses.put(clazz, classesArray);
                    addCollectedSuperclasses(superclasses, classesArray);
                }
            }
        }
    }

}
