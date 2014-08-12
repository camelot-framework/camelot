package ru.yandex.qatools.camelot.api.annotations;

import ru.yandex.qatools.camelot.api.CustomFilter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the splitting strategy before aggregation
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {

    /**
     * Indicates the split class
     */
    Class[] instanceOf() default {Object.class};

    /**
     * Indicates the custom filter class
     */
    Class<? extends CustomFilter> custom() default CustomFilter.class;
}
