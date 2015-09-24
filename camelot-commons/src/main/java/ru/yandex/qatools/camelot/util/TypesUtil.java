package ru.yandex.qatools.camelot.util;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class TypesUtil {

    TypesUtil() {
    }

    /**
     * Checks if type is assignable from type considering the primitive types
     *
     * @param type - java type
     * @return true if the given type is integer
     */
    public static boolean isAssignableFrom(Class<?> type, Class<?> fromType) { //NOSONAR
        return type.isAssignableFrom(fromType) || //NOSONAR
                (isInt(fromType) && isInt(type)) ||
                (isLong(fromType) && isLong(type)) ||
                (isDouble(fromType) && isDouble(type)) ||
                (isBoolean(fromType) && isBoolean(type)) ||
                (isFloat(fromType) && isFloat(type));
    }

    /**
     * Checks if the type is long
     *
     * @param type - java type
     * @return true if the given type is long
     */
    public static boolean isLong(Class<?> type) {
        return Long.class.isAssignableFrom(type) || (type.isPrimitive() && Long.TYPE.equals(type));
    }

    /**
     * Checks if the type is integer
     *
     * @param type - java type
     * @return true if the given type is integer
     */
    public static boolean isInt(Class<?> type) {
        return Integer.class.isAssignableFrom(type) || (type.isPrimitive() && Integer.TYPE.equals(type));
    }

    /**
     * Checks if the type is double
     *
     * @param type - java type
     * @return true if the given type is double
     */
    public static boolean isDouble(Class<?> type) {
        return Double.class.isAssignableFrom(type) || (type.isPrimitive() && Double.TYPE.equals(type));
    }

    /**
     * Checks if the type is float
     *
     * @param type - java type
     * @return true if the given type is float
     */
    public static boolean isFloat(Class<?> type) {
        return Float.class.isAssignableFrom(type) || (type.isPrimitive() && Float.TYPE.equals(type));
    }

    /**
     * Checks if the type is Boolean
     *
     * @param type - java type
     * @return true if the given type is boolean
     */
    public static boolean isBoolean(Class<?> type) {
        return Boolean.class.isAssignableFrom(type) || (type.isPrimitive() && Boolean.TYPE.equals(type));
    }
}
