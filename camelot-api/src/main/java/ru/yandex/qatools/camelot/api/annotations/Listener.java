package ru.yandex.qatools.camelot.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the field to inject the plugin's output listener
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Listener {
    Type value() default Type.PLUGIN_OUTPUT;

    public static enum Type {
        PLUGIN_OUTPUT
    }

}
