package ru.yandex.qatools.camelot.serialize.fst;

import org.apache.camel.util.LRUSoftCache;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.camelot.common.BasicMessagesSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import static org.nustaq.serialization.FSTConfiguration.createDefaultConfiguration;
import static ru.yandex.qatools.camelot.util.SerializeUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotFastMessagesSerializer extends BasicMessagesSerializer {

    public static final int MAX_CACHE_SIZE = 1000;
    private static final Map<ClassLoader, FSTConfiguration> configByClassLoader = new LRUSoftCache<>(MAX_CACHE_SIZE);
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Serialize the object to bytes
     */
    public byte[] serializeToBytes(Object object, ClassLoader classLoader) {
        if (object == null || classLoader == null) {
            return null; //NOSONAR
        }
        try {
            FSTConfiguration config = configByClassLoader.get(classLoader);
            if (config == null) {
                config = createDefaultConfiguration();
                configByClassLoader.put(classLoader, config);
                config.setClassLoader(classLoader);
            }
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            FSTObjectOutput out = config.getObjectOutput(outStream);
            out.writeObject(object, object.getClass());
            out.flush();
            return outStream.toByteArray();
        } catch (Exception e) {
            LOGGER.warn("Failed to serialize object to bytes", e);
            return null; //NOSONAR
        }

    }

    /**
     * Deserialize the input bytes into object
     */
    public Object deserializeFromBytes(byte[] input, ClassLoader classLoader, Class expectedClass) throws Exception { //NOSONAR
        ByteArrayInputStream bis = new ByteArrayInputStream(input);
        FSTConfiguration config = configByClassLoader.get(classLoader);
        if (config == null) {
            config = createDefaultConfiguration();
            configByClassLoader.put(classLoader, config);
            config.setClassLoader(classLoader);
        }
        return config.getObjectInput(bis).readObject(expectedClass);
    }

    @Override
    public Object deserialize(Object body, ClassLoader classLoader) {
        try {
            if (body != null) {
                final Class<?> bodyClass = Class.forName(unwrapBodyClassName((byte[]) body));
                return deserializeFromBytes(unwrapBytesWithMeta((byte[]) body), classLoader, bodyClass);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to deserialize message from bytes: " + e.getMessage(), e);
        }
        return body;
    }

    @Override
    public Object serialize(Object body, ClassLoader classLoader) {
        if (body != null) {
            final byte[] bytes = serializeToBytes(body, classLoader);
            return (bytes != null) ? wrapBytesWithMeta(bytes, body.getClass().getName()) : null;
        }
        return null;
    }
}
