package ru.yandex.qatools.camelot.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface MetadataClassInfo<T> {

    Method[] getAnnotatedMethods(Class aClass);

    Class[] getSuperClasses(Class clazz);

    Collection<Method> getMethodsByParamTypes(Class<? extends Annotation> aClass, Class... paramType);

}
