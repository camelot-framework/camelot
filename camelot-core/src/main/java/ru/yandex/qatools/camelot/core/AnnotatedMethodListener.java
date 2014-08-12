package ru.yandex.qatools.camelot.core;

import java.lang.reflect.Method;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AnnotatedMethodListener<T, A> {

    T found(Method method, A annotation) throws Exception;

}
