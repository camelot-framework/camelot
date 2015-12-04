package ru.yandex.qatools.camelot.mongodb;

import ru.qatools.mongodb.Deserializer;
import ru.qatools.mongodb.Serializer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;

import java.io.Serializable;

/**
 * @author Ilya Sadykov
 */
@SuppressWarnings("unchecked")
public class MongoSerializer implements Serializer, Deserializer {
    final MessagesSerializer serializer;

    public MongoSerializer(MessagesSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    public <T extends Serializable> T fromBytes(byte[] input, ClassLoader classLoader) throws Exception {
        return (T) serializer.deserialize(input, classLoader);
    }

    @Override
    public byte[] toBytes(Object object, ClassLoader classLoader) {
        return (byte[]) serializer.serialize(object, classLoader);
    }
}
