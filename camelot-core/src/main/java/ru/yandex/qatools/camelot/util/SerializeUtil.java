package ru.yandex.qatools.camelot.util;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static java.lang.Thread.currentThread;

public class SerializeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);

    /**
     * Serialize the object to bytes
     */
    public static byte[] serializeToBytes(Object object) {
        if (object == null) {
            return null;
        }
        return serializeToBytes(object, (object.getClass().getClassLoader() != null) ?
                object.getClass().getClassLoader() : currentThread().getContextClassLoader());
    }

    /**
     * Serialize the object to bytes
     */
    public static byte[] serializeToBytes(Object object, ClassLoader classLoader) {
        if (object == null || classLoader == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new ObjectOutputStream(bos).writeObject(object);
            return bos.toByteArray();
        } catch (Exception e) {
            logger.error("Failed to serialize object to bytes", e);
            return null;
        }
    }

    /**
     * Deserialize the input bytes into object
     */
    public static <T extends Serializable> T deserializeFromBytes(byte[] input, Class<T> expectedClass) throws Exception {
        return deserializeFromBytes(input, expectedClass.getClassLoader(), expectedClass);
    }

    /**
     * Deserialize the input bytes into object
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserializeFromBytes(byte[] input, ClassLoader classLoader, Class<T> expectedClass) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        return (T) new ClassLoaderObjectInputStream(classLoader, bis).readObject();
    }

    /**
     * Perform the check of the input object if it needs the deserialization and try to deserialize
     *
     * @return null if the input object could not be deserialized | input object if it does not need the deserialization
     */
    public static <T extends Serializable> T checkAndGetBytesInput(Class<T> type, Object input) {
        return checkAndGetBytesInput(type, input, type.getClassLoader());
    }

    /**
     * Perform the check of the input object if it needs the deserialization and try to deserialize
     *
     * @return null if the input object could not be deserialized | input object if it does not need the deserialization
     */
    public static <T extends Serializable> T checkAndGetBytesInput(Class<T> type, Object input, ClassLoader classLoader) {
        T res = null;
        try {
            if (input instanceof byte[]) {
                res = deserializeFromBytes((byte[]) input, classLoader, type);
            } else if (type.isAssignableFrom(input.getClass())) {
                res = (T) input;
            }
        } catch (Exception ignored) {
            logger.trace("Ignored exception", ignored);
        }
        if (res != null && res.getClass() != null && type.isAssignableFrom(res.getClass())) {
            return res;
        }
        System.err.println("Cannot deserialize bytes to type: " + type);
        return null;
    }

}