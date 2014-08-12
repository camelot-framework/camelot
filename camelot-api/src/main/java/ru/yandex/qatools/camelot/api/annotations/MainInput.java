package ru.yandex.qatools.camelot.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the field to inject the producer for the common (INPUT) queue
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MainInput {
}
