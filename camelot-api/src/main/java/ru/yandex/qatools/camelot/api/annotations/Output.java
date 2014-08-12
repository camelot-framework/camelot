package ru.yandex.qatools.camelot.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the field to inject the output producer for the plugin's output
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Output {

    /**
     * Indicates the class of the plugin to be injected
     */
    Class value() default Object.class;

    /**
     * Indicates the id of the plugin to be injected
     */
    String id() default "";
}
