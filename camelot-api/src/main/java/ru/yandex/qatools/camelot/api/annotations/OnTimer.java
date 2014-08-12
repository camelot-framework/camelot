package ru.yandex.qatools.camelot.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the method, that will be called on timer
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnTimer {
    /**
     * Indicates the cron schedule for the timer in the Quartz format
     */
    String cron() default "";

    /**
     * Indicates the method within aggregator to be called when it is needed to generate the cron string
     */
    String cronMethod() default "";

    /**
     * Indicates that we should skip execution of this task if the previous execution is still executing
     * default: false
     */
    boolean skipIfNotCompleted() default false;

    /**
     * Indicates whatever or not we should use the state exclusively (otherwise the state will be readonly).
     * default: true
     */
    boolean readOnly() default true;


    /**
     * Indicates if this timer must be used for each state or it's a global timer used for the aggregator itself
     * default: true
     */
    boolean perState() default true;
}
