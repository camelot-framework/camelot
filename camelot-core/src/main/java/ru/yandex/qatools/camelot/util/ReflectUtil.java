package ru.yandex.qatools.camelot.util;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ClassUtils.getAllInterfaces;

/**
 * Util class allowing to scan all the classes inside the specified package and other class operations
 * User: isadykov
 * Date: 16.03.12
 * Time: 15:55
 */
public class ReflectUtil {

    private static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);

    public static interface ExceptionHandler {
        Object handleException(Throwable e) throws Throwable;
    }

    public static String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }


    /**
     * Create a proxy object implementing some interface from a specified class loader
     */
    @SuppressWarnings("unchecked")
    public static <C, I extends C> I classLoaderProxy(ClassLoader cl, C object, Class<I> classInterface)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return (I) Proxy.newProxyInstance(classInterface.getClassLoader(), new Class[]{classInterface}, new ThroughClassLoaderProxyHandler(object, cl));
    }


    /**
     * An invocation handler that passes on any calls made to it directly to its delegate.
     * This is useful to handle identical classes loaded in different classloaders - the
     * VM treats them as different classes, but they have identical signatures.
     * <p/>
     * Note this is using class.getMethod, which will only work on public methods.
     * Note this is using the arguments that are of primitive types/ types from java.lang / classes implementing one
     * interface exactly
     */
    private static class ThroughClassLoaderProxyHandler implements InvocationHandler {
        private final Object delegate;
        private final ClassLoader guestClassLoader;
        private final ClassLoader hostClassLoader;
        private final ExceptionHandler exceptionHandler;

        public ThroughClassLoaderProxyHandler(Object delegate, ClassLoader classLoader, ExceptionHandler handler) {
            this.delegate = delegate;
            this.guestClassLoader = classLoader;
            this.hostClassLoader = getClass().getClassLoader();
            this.exceptionHandler = handler;
        }

        public ThroughClassLoaderProxyHandler(Object delegate, ClassLoader classLoader) {
            this(delegate, classLoader, null);
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                Method delegateMethod = delegate.getClass().getMethod(method.getName(),
                        wrapTypesForClassLoader(guestClassLoader, method.getParameterTypes()));
                return wrapObjectForClassLoader(
                        hostClassLoader,
                        guestClassLoader,
                        delegateMethod.invoke(delegate,
                                wrapArgsForClassLoader(guestClassLoader, hostClassLoader, delegateMethod, args)
                        ),
                        null
                );
            } catch (Exception e) {
                logger.error("Failed to invoke method " + method.getName() + " on proxy " + proxy, e);
                return throwRootExceptionFromClassLoader(hostClassLoader, e, exceptionHandler);
            }
        }
    }

    /**
     * Collect all interfaces of a class
     */
    private static Set<Class> collectAllClassInterfaces(final Class objClazz) {
        Set<Class> interfaces = new HashSet<>();
        Class clazz = objClazz;
        // search through superclasses
        while (clazz != null) {
            interfaces.addAll(asList(clazz.getInterfaces()));
            clazz = clazz.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Throw an exception which is a root cause of the problem, creating the same instance in the host class loader
     */
    @SuppressWarnings("unchecked")
    private static Object throwRootExceptionFromClassLoader(ClassLoader cl, Exception e, ExceptionHandler handler)
            throws Throwable {
        Throwable rootE = getRootException(e);
        Class<? extends Throwable> eClass = (Class<? extends Throwable>) cl.loadClass(rootE.getClass().getName());
        Throwable resultException = e;
        try {
            resultException = eClass.getConstructor(Throwable.class).newInstance(rootE);
        } catch (NoSuchMethodException ignored) {
            logger.trace("Ignored exception", ignored);
            try {
                resultException = (eClass.getConstructor(String.class).newInstance(rootE.getMessage()));
            } catch (NoSuchMethodException ignored2) {
                logger.trace("Ignored exception", ignored2);
                resultException = eClass.newInstance();
            }
        }
        if (handler != null) {
            return handler.handleException(resultException);
        }
        resultException.setStackTrace(rootE.getStackTrace());
        throw resultException;
    }

    /**
     * Retrieve the root exception through getCause()
     */
    private static Throwable getRootException(Exception e) {
        Throwable rootE = e;
        while (rootE.getCause() != null) {
            rootE = rootE.getCause();
        }
        return rootE;
    }

    /**
     * Load type's class from a specified classloader.
     * If a type is basic or from basic "java" package return itself.
     */
    private static Class<?> classLoaderType(ClassLoader cl, Class<?> type) throws ClassNotFoundException {
        return isBasicJavaType(type) ? type : cl.loadClass(type.getName());
    }

    /**
     * returns true if a type is a basic java type
     */
    private static boolean isBasicJavaType(Class<?> type) {
        return type.getPackage() == null || type.getPackage().getName().startsWith("java");
    }

    /**
     * Get parameter types list from a class loader.
     */
    private static Class<?>[] wrapTypesForClassLoader(ClassLoader cl, Class<?>[] types)
            throws ClassNotFoundException {
        List<Class<?>> res = new ArrayList<>();
        if (types != null) {
            for (Class<?> type : types) {
                Class<?> clazz = classLoaderType(cl, type);
                res.add(clazz);
            }
        }
        return res.toArray(new Class<?>[res.size()]);
    }

    /**
     * Wrap args for a class loader (if necessary)
     *
     * @param hostCL  specifies a host class loader (from which argument is accessible)
     * @param method  specifies a method that should be used for wrapping
     * @param guestCL specifies a guest class loader (which is supposed to be the actual consumer of the arguments)
     */
    private static Object[] wrapArgsForClassLoader(ClassLoader hostCL, ClassLoader guestCL, Method method, Object[] args)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        List<Object> wrappedArgs = new ArrayList<>();
        if (args != null) {
            if (method.getParameterTypes().length != args.length) {
                throw new RuntimeException("Cannot wrap arguments: method " + method.getName() + " is expecting " +
                        method.getParameterTypes().length + " args, but only " + args.length + " provided!");
            }
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                wrappedArgs.add(wrapObjectForClassLoader(hostCL, guestCL, args[i], method.getParameterTypes()[i]));
            }
        }
        return wrappedArgs.toArray(new Object[wrappedArgs.size()]);
    }

    /**
     * Wrap object for a class loader (if necessary)
     *
     * @param hostCL       specifies a host class loader (from which argument is accessible)
     * @param guestCL      specifies a guest class loader (which is supposed to be the actual consumer of the arguments)
     * @param originalType type that must be used for wrapping
     */
    private static Object wrapObjectForClassLoader(ClassLoader hostCL, ClassLoader guestCL, Object arg, Class originalType)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (arg == null) {
            return null;
        }
        if (arg instanceof Collection) {
            Collection newList = instantiateCollection((Class<? extends Collection>) arg.getClass());
            for (Object item : (Collection) arg) {
                newList.add(wrapObjectForClassLoader(hostCL, guestCL, item, originalType));
            }
            return newList;
        } else if (arg instanceof Enum) {
            return EnumUtil.fromString((Class<Enum>) guestCL.loadClass(arg.getClass().getName()), ((Enum) arg).name());
        } else if (!isBasicJavaType(arg.getClass())) {
            if (originalType != null) {
                return classLoaderProxy(hostCL, arg, originalType);
            }
            // if this is a not a basic type or a type from java base package, we must create proxy for it (if it
            // implements exactly one interface)
            return classLoaderProxy(hostCL, arg, guestCL.loadClass(getClassSingleInterface(arg.getClass()).getName()));
        } else if (arg instanceof Class) {
            // this is a class, we must load it from guest CL
            return guestCL.loadClass(((Class) arg).getName());
        } else {
            // this is probably a primitive type or basic class, we can pass it through as it is
            return (arg);
        }
    }

    /**
     * Instantiate the collection by its class
     *
     * @param collClass
     * @return
     */
    public static Collection instantiateCollection(Class<? extends Collection> collClass) throws IllegalAccessException, InstantiationException {
        if (List.class.isAssignableFrom(collClass)) {
            return new ArrayList();
        } else if (Set.class.isAssignableFrom(collClass)) {
            return new HashSet();
        } else if (!collClass.isInterface() && !Modifier.isAbstract(collClass.getModifiers())) {
            return collClass.newInstance();
        } else {
            throw new RuntimeException("Cannot instantiate collection of a class: " + collClass + ": Not supported!");
        }
    }


    /**
     * Returns single interface for a class.
     * Throws an exception if a class has more than 1 or has no interfaces
     */
    private static <T> Class<?> getClassSingleInterface(Class<T> clazz) {
        List<Class<?>> interfaces = getNonBasicJavaInterfaces(clazz, false);
        if (interfaces.size() != 1) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " must implement exactly one non-basic java interface!");
        }
        return interfaces.get(0);
    }

    /**
     * Returns non-basic java interfaces of a class
     */
    private static List<Class<?>> getNonBasicJavaInterfaces(Class<?> clazz, boolean includeSuperClasses) {
        List<Class<?>> result = new ArrayList<>();
        final List allInterfaces = (includeSuperClasses) ? getAllInterfaces(clazz) : asList(clazz.getInterfaces());
        for (Object iface : allInterfaces) {
            if (!isBasicJavaType((Class<?>) iface)) {
                result.add((Class<?>) iface);
            }
        }
        return result;
    }

    /**
     * Returns the list of classes for the arguments from the different classloader
     */
    public static Class<?>[] getArgTypes(Object[] arguments, ClassLoader classLoader) throws ClassNotFoundException {
        List<Class<?>> types = new ArrayList<>();
        for (Object arg : arguments) {
            types.add(classLoader.loadClass(arg.getClass().getName()));
        }
        return types.toArray(new Class<?>[types.size()]);
    }


    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param basePackage base package name
     * @return list of the classes within base package
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Set<Class<?>> getClasses(String basePackage) throws IOException,
            ClassNotFoundException {
        return getClasses(basePackage, ReflectUtil.class);
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param basePackage base package name
     * @return list of the classes within base package
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Set<Class<?>> getClasses(String basePackage, Class<?> baseClass) throws IOException,
            ClassNotFoundException {
        final ClassLoader classLoader = baseClass.getClassLoader();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(classLoader);

        Set<Class<?>> candidates = new HashSet<>();
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/" + "**/*.class";
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        Resource[] resources = resolver.getResources(packageSearchPath);
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                candidates.add(classLoader.loadClass(metadataReader.getClassMetadata().getClassName()));
            }
        }
        return candidates;
    }


    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    public static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                assert !file.getName().contains("");
                classes.addAll(findClasses(file, packageName + "" + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of Strings URLs of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<String> resolveResourcesAsStringsFromPattern(final String pattern) throws IOException,
            ClassNotFoundException {
        return resolveResourcesAsStringsFromPattern(pattern, ReflectUtil.class);
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of Strings URLs of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<String>  resolveResourcesAsStringsFromPattern(final String pattern,
                                                                          final Class<?> baseClass)
            throws IOException, ClassNotFoundException {
        final ClassLoader classLoader = baseClass.getClassLoader();
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        final Collection<String> classes = new LinkedList<>();
        final Resource[] resources = resolver.getResources(pattern);
        for (final Resource resource : resources) {
            final URL url = resource.getURL();
            classes.add(url.toString());
        }
        return classes;
    }


    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern)
            throws IOException, ClassNotFoundException {
        return resolveResourcesFromPattern(pattern, ReflectUtil.class);
    }


    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern, final ClassLoader classLoader)
            throws IOException, ClassNotFoundException {
        final Collection<Resource> classes = new LinkedList<>();
        final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(classLoader);
        final Resource[] resources = resolver.getResources(pattern);
        Collections.addAll(classes, resources);
        return classes;
    }

    /**
     * Returns list of resources defined in classpath by a pattern
     *
     * @param pattern pattern
     * @return list of resources
     * @throws java.io.IOException
     * @throws ClassNotFoundException
     */
    public static Collection<Resource> resolveResourcesFromPattern(final String pattern, final Class<?> baseClass)
            throws IOException, ClassNotFoundException {
        return resolveResourcesFromPattern(pattern, baseClass.getClassLoader());
    }

    /**
     * Searches for a certain annotation in the class hierarchy
     *
     * @param annotatedClass
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T findAnnotationInClassHierarchy(Class<?> annotatedClass, Class<T> annotationClass) {
        T result = null;
        while (annotatedClass != null && result == null) {
            result = annotatedClass.getAnnotation(annotationClass);
            annotatedClass = annotatedClass.getSuperclass();
        }
        return result;
    }

    /**
     * Searches for all fields within class hierarchy
     *
     * @return
     */
    public static Field[] getFieldsInClassHierarchy(Class<?> clazz) {
        Field[] fields = {};
        while (clazz != null) {
            fields = ArrayUtils.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * Searches for the method within class hierarchy
     *
     * @return
     */
    public static Method getMethodFromClassHierarchy(Class<?> clazz, String methodName) throws NoSuchMethodException {
        while (clazz != null) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new NoSuchMethodException(methodName);
    }

    /**
     * Searches for all methods within class hierarchy
     *
     * @return
     */
    public static Method[] getMethodsInClassHierarchy(Class<?> clazz) {
        Method[] methods = {};
        while (clazz != null) {
            methods = ArrayUtils.addAll(methods, clazz.getDeclaredMethods());
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    /**
     * Returns the generic arguments from the field
     *
     * @param field - field to be reflect
     * @return Type arguments array
     */
    public static Type[] getFieldTypeArguments(Field field) {
        Type genericFieldType = field.getGenericType();
        if (genericFieldType instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) genericFieldType;
            return aType.getActualTypeArguments();
        }
        return new Type[]{};
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(Class<?> clazz, T instance, String method, Class<?>[] argTypes,
                                             Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<Class<?>> types = new ArrayList<>();
        if (argTypes == null) {
            for (Object arg : arguments) {
                types.add(arg.getClass());
            }
            argTypes = types.toArray(new Class<?>[types.size()]);
        }
        Method m;
        try {
            m = clazz.getMethod(method, argTypes);
        } catch (NoSuchMethodException ignored) {
            logger.trace("Ignored exception", ignored);
            m = clazz.getDeclaredMethod(method, argTypes);
        }
        m.setAccessible(true);
        return m.invoke(instance, arguments);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Class<?>[] argTypes, Object... arguments) throws
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        return invokeAnyMethod(instance.getClass(), instance, method, argTypes, arguments);
    }

    /**
     * Set private field
     */
    public static <T> void setPrivateField(T instance, String name, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = (instance instanceof Class)
                ? ((Class) instance).getDeclaredField(name)
                : instance.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }

    /**
     * Invokes any object method (even if it's private)
     */
    public static <T> Object invokeAnyMethod(T instance, String method, Object... args) throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        return invokeAnyMethod(instance, method, null, args);
    }


    /**
     * Get annotation value of annotation object via reflection
     */
    public static Object getAnnotationValue(Object aObj, String aValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return aObj.getClass().getMethod(aValue).invoke(aObj);
    }

    /**
     * Get annotation value of annotation object via reflection
     */
    public static Object getAnnotationValue(AnnotatedElement aobj, Class aClass, String aValue) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return getAnnotationValue(getAnnotation(aobj, aClass), aValue);
    }

    /**
     * Get annotation of an object via reflection
     */
    public static Object getAnnotation(AnnotatedElement aobj, Class aClass) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (Object a : aobj.getAnnotations()) {
            if (isAnnotationInstance(aClass, a)) return a;
        }
        return null;
    }

    /**
     * Get annotation within hierarchy
     */
    public static <A extends Annotation> Object getAnnotationWithinHierarchy(Class<?> fsmClass, Class<A> aggregateClass) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        while (fsmClass != null) {
            if (getAnnotation(fsmClass, aggregateClass) != null) {
                return getAnnotation(fsmClass, aggregateClass);
            }
            fsmClass = fsmClass.getSuperclass();
        }
        return null;
    }

    private static boolean isAnnotationInstance(Class aClass, Object a) {
        if (Proxy.isProxyClass(a.getClass())) {
            for (Class aInterface : a.getClass().getInterfaces()) {
                if (aInterface.getName().equals(aClass.getName())) {
                    return true;
                }
            }
        }
        return aClass.isInstance(a);
    }

}
