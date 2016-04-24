package ru.yandex.qatools.camelot.mongodb;

import ru.yandex.qatools.camelot.common.MessagesSerializer;

/**
 * @author Ilya Sadykov
 */
public class MongoSerializerBuilder {
    public MongoSerializer build(MessagesSerializer msgSerializer, ClassLoader classLoader) {
        return new MongoSerializerBinary(msgSerializer, classLoader);
    }
}
