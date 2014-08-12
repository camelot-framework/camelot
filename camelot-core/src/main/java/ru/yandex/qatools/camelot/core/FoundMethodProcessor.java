package ru.yandex.qatools.camelot.core;

import java.lang.reflect.Method;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface FoundMethodProcessor<A> {
    /**
     * Check if the method with such annotation object should be used within the processor
     */
    boolean appliesTo(Method method, A annotation);

}

