package ru.yandex.qatools.camelot.core.kafka;

import ru.yandex.qatools.camelot.core.impl.BasicMessagesSerializer;

import java.io.Serializable;

import static ru.yandex.qatools.camelot.util.SerializeUtil.*;

/**
 * @author Ilya Sadykov
 */
public class KafkaMessagesSerializer extends BasicMessagesSerializer {
    @Override
    @SuppressWarnings("unchecked")
    public Object deserialize(Object body, ClassLoader classLoader) {
        try {
            if (body != null) {
                final Class<? extends Serializable> expClass = (Class<? extends Serializable>) Class.forName(
                        identifyBodyClassName(body), false, classLoader);
                final byte[] unwrapped = unwrapBytesWithMeta((byte[]) body);
//                final byte[] unwrappedBytes = parseBase64Binary(new String(unwrapBytesWithMeta((byte[]) body), UTF_8));
//            logger.info("Received bytes follows: \n\n\n\n\n{}", Arrays.toString(unwrappedBytes));
//                return deserializeFromBytes(unwrappedBytes, classLoader, expClass);
                return deserializeFromBytes(unwrapped, classLoader, expClass);
            }
        } catch (Exception e) {
            logger.error("Failed to deserialize message from String", e);
        }
        return body;
    }

    @Override
    public Object serialize(Object body, ClassLoader classLoader) {
        if (body != null) {
            final byte[] bytes = serializeToBytes(body, classLoader);
//        logger.info("Serialized object follows: \n\n\n\n\n{}", byteArrayToPretty(unwrappedBytes));
//            return new String(wrapBytesWithMeta(printBase64Binary(bytes).getBytes(UTF_8), body.getClass().getName()), UTF_8);
            return wrapBytesWithMeta(bytes, body.getClass().getName());
        }
        return null;
    }
}
