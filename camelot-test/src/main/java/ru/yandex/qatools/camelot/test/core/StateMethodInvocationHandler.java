package ru.yandex.qatools.camelot.test.core;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.clay.utils.lang3.NoClassNameStyle;

import java.lang.reflect.Method;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

/**
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
class StateMethodInvocationHandler implements MethodInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final StateLoader stateStorage;

    private final Class stateClass;

    private final String key;

    public StateMethodInvocationHandler(StateLoader stateStorage, Class stateClass, String key) {
        this.stateStorage = stateStorage;
        this.stateClass = stateClass;
        this.key = key;
    }

    @Override
    public Object intercept(Object o,
                            Method method,
                            Object[] objects,
                            MethodProxy methodProxy) throws Throwable {

        Object fetchedObject = stateStorage.fetchState(key);
        if (isToString(method)) {
            return stateClass.getSimpleName()
                    + reflectionToString(fetchedObject, new NoClassNameStyle());
        }

        if (fetchedObject == null) {
            try {
                fetchedObject = stateClass.newInstance();
            } catch (InstantiationException
                    | IllegalAccessException
                    | ExceptionInInitializerError
                    | SecurityException e) {
                logger.warn("Unable to find the desired state in the repo " +
                        "and unable to instantiate a new one with an empty " +
                        "public constructor. Will have to return null as " +
                        "a result of " + method.getName() + " invocation!", e);
                return null;
            }
        }

        return method.invoke(fetchedObject, objects);
    }

    private boolean isToString(Method method) {
        return method.getName().equals("toString")
                && method.getTypeParameters().length == 0;
    }
}
