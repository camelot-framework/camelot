package ru.yandex.qatools.camelot.common;

import java.lang.annotation.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScanMethodsAnnotatedWith {

    Class<? extends Annotation>[] value();

}
