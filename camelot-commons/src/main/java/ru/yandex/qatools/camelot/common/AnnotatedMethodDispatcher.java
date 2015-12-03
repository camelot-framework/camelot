package ru.yandex.qatools.camelot.common;

import ru.yandex.qatools.camelot.error.DispatchException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class AnnotatedMethodDispatcher {

    private final Object instance;
    private final MetadataClassInfo cache;

    public AnnotatedMethodDispatcher(Object instance, MetadataClassInfo meta) {
        this.cache = meta;
        this.instance = instance;
    }

    public Map<Method, Object> dispatch(Class<? extends Annotation> annClass, boolean singleCall, Object... params) throws Exception { //NOSONAR
        final List<Class> paramTypes = new ArrayList<>();
        final List<Object> paramList = new ArrayList<>();
        for (Object value : params) {
            paramTypes.add(value.getClass());
            paramList.add(value);
        }
        if (paramTypes.isEmpty()) {
            throw new DispatchException(format("Failed to invoke methods annotated with @%s: parameters are empty!", annClass));
        }
        final Map<Method, Object> called = new HashMap<>();
        for (int i = 0; i <= paramTypes.size(); ++i) {
            for (int j = paramTypes.size(); j >= i; --j) {
                final List<Class> typesSubList = paramTypes.subList(i, j);
                final Deque<Class[]> typesStack = new ArrayDeque<>();
                typesStack.push(typesSubList.toArray(new Class[typesSubList.size()]));
                call(annClass, typesStack, paramList.subList(i, j), singleCall, called, 0);
                if (singleCall && !called.isEmpty()) {
                    return called;
                }
            }
        }
        return called;
    }


    protected void call(Class<? extends Annotation> annClass, Deque<Class[]> typesStack, List<Object> params,
                        boolean singleCall, Map<Method, Object> called, int paramIdx) throws Exception { //NOSONAR
        try {
            if (singleCall && !called.isEmpty()) {
                return;
            }
            List<Class> paramTypes = new ArrayList<>();
            paramTypes.addAll(asList(typesStack.peek()));
            if (paramIdx >= paramTypes.size() || paramTypes.get(paramIdx) == Object.class) {
                return;
            }
            for (Class paramType : cache.getSuperClasses(paramTypes.get(paramIdx))) {
                paramTypes.set(paramIdx, paramType);

                findSuitableMethodAndCall(annClass, paramTypes, params, singleCall, called);
                typesStack.push(paramTypes.toArray(new Class[paramTypes.size()]));
                call(annClass, typesStack, params, singleCall, called, paramIdx + 1);
                typesStack.pop();
                if (singleCall && !called.isEmpty()) {
                    return;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw wrapThrowableIfRequired(e, e.getCause());
        } catch (InvocationTargetException e) {
            throw wrapThrowableIfRequired(e, e.getTargetException());
        }
    }

    private Exception wrapThrowableIfRequired(Throwable e, Throwable cause) throws Exception { //NOSONAR
        if (cause == null) {
            return new Exception(e);
        }
        return (cause instanceof Exception) ? (Exception) cause : new Exception(cause);
    }

    @SuppressWarnings("unchecked")
    private void findSuitableMethodAndCall(Class<? extends Annotation> annClass, List<Class> paramTypes, List<Object> params,
                                           boolean singleCall, Map<Method, Object> called) throws Exception { //NOSONAR
        final Class[] mParamTypes = paramTypes.toArray(new Class[paramTypes.size()]);
        final Collection<Method> methods = cache.getMethodsByParamTypes(annClass, mParamTypes);
        for (Method method : methods) {
            if (checkMethodParams(method.getParameterTypes(), paramTypes)) {
                callMethod(method, paramTypes, params, called);
                if (singleCall && !called.isEmpty()) {
                    return;
                }
            }
        }
    }

    private void callMethod(Method method, List<Class> types, List<Object> params, Map<Method, Object> called) throws IllegalAccessException, InvocationTargetException {
        if (types.size() == method.getParameterTypes().length && !called.containsKey(method)) {
            called.put(
                    method,
                    method.invoke(instance, params.toArray(new Object[types.size()]))
            );
        }
    }

    private boolean checkMethodParams(Class[] paramTypes, List<Class> types) {
        if (types.size() > paramTypes.length) {
            return false;
        }
        for (int i = 0; i < types.size(); ++i) {
            if (!paramTypes[i].equals(types.get(i))) {
                return false;
            }
        }
        return true;
    }
}
