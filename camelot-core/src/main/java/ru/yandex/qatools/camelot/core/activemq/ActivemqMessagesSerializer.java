package ru.yandex.qatools.camelot.core.activemq;

import ru.yandex.qatools.camelot.core.impl.BasicMessagesSerializer;

import java.io.Serializable;

import static ru.yandex.qatools.camelot.util.SerializeUtil.*;

/**
 * @author Ilya Sadykov
 */
public class ActivemqMessagesSerializer extends BasicMessagesSerializer {

    @Override
    public Object deserialize(Object body, ClassLoader classLoader) {
        try {
            if (body != null) {
                final Class<? extends Serializable> expClass = (Class<? extends Serializable>) Class.forName(
                        identifyBodyClassName(body), false, classLoader);
                return deserializeFromBytes(unwrapBytesWithMeta((byte[]) body),
                        classLoader, expClass);
            }
        } catch (Exception e) {
            logger.debug("Failed to deserialize message from bytes: " + e.getMessage(), e);
        }
        return body;
    }

    @Override
    public Object serialize(Object body, ClassLoader classLoader) {
        if (body != null) {
            return wrapBytesWithMeta(serializeToBytes(body, classLoader), body.getClass().getName());
        }
        return null;
    }


}
