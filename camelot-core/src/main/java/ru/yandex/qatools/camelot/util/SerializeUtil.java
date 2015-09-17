package ru.yandex.qatools.camelot.util;

import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.core.MessagesSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import static java.lang.System.arraycopy;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.copyOfRange;
import static java.util.Arrays.fill;

public class SerializeUtil {
    private static final Logger logger = LoggerFactory.getLogger(SerializeUtil.class);
    public static final int META_HEADER_SIZE_BYTES = 100;


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
    public static byte[] unwrapBytesWithMeta(byte[] bytes) {
        final byte[] res = new byte[bytes.length - META_HEADER_SIZE_BYTES];
        arraycopy(bytes, META_HEADER_SIZE_BYTES, res, 0, res.length);
        return res;
    }

    public static String unwrapBodyClassName(byte[] bytes) {
        final String meta = new String(copyOfRange(bytes, 0, META_HEADER_SIZE_BYTES));
        return meta.substring(0, meta.indexOf(0));
    }

    public static byte[] wrapBytesWithMeta(byte[] bytes, String bodyClassName) {
        final byte[] res = new byte[bytes.length + META_HEADER_SIZE_BYTES];
        arraycopy(bytes, 0, res, META_HEADER_SIZE_BYTES, bytes.length);
        arraycopy(bodyClassName.getBytes(), 0, res, 0, bodyClassName.length());
        fill(res, bodyClassName.length(), META_HEADER_SIZE_BYTES, (byte) 0);
        return res;
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
     * Perform the check of the input object if it needs the deserialization and try to fromBytes
     *
     * @return null if the input object could not be deserialized | input object if it does not need the deserialization
     */
    public static <T extends Serializable> T checkAndGetBytesInput(Class<T> type, Object input, MessagesSerializer serializer) {
        return checkAndGetBytesInput(type, input, serializer, type.getClassLoader());
    }

    /**
     * Perform the check of the input object if it needs the deserialization and try to fromBytes
     *
     * @return null if the input object could not be deserialized | input object if it does not need the deserialization
     */
    public static <T extends Serializable> T checkAndGetBytesInput(Class<T> type, Object input,
                                                                   MessagesSerializer serializer, ClassLoader classLoader) {
        T res = null;
        try {
            res = (T) serializer.processBodyAndHeadersAfterReceive(input, new HashMap<String, Object>(), classLoader);
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