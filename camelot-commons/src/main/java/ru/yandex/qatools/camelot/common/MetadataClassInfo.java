package ru.yandex.qatools.camelot.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface MetadataClassInfo {

    Method[] getAnnotatedMethods(Class aClass);

    Class[] getSuperClasses(Class clazz);

    Collection<Method> getMethodsByParamTypes(Class<? extends Annotation> aClass, Class... paramType);

}
