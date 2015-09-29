package ru.yandex.qatools.camelot.common;

import java.lang.reflect.Field;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface AnnotatedFieldListener<T, U> {

    T found(Field field, U annotation) throws Exception; //NOSONAR

}
