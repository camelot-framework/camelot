package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.BasicDBObject;
import org.bson.Document;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.Deserializer;
import ru.qatools.mongodb.Serializer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.util.SerializeUtil;

/**
 * @author Ilya Sadykov
 */
@SuppressWarnings("unchecked")
public class MongoSerializer implements Serializer, Deserializer {
    public static final String OBJECT_FIELD = "object";
    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeUtil.class);
    final MessagesSerializer serializer;
    final ClassLoader classLoader;

    public MongoSerializer(MessagesSerializer serializer, ClassLoader classLoader) {
        this.serializer = serializer;
        this.classLoader = classLoader;
    }

    /**
     * Serialize the object to bytes
     */
    @Override
    public BasicDBObject toDBObject(Object object) {
        try {
            return new BasicDBObject(OBJECT_FIELD, serializer.serialize(object, classLoader));
        } catch (Exception e) {
            LOGGER.error("Failed to serialize object to bytes", e);
            return new BasicDBObject("object", null); //NOSONAR
        }
    }

    /**
     * Deserialize the input bytes into object
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T fromDBObject(Document input, Class<T> expected)
            throws Exception { //NOSONAR
        final Object value = input.get(OBJECT_FIELD);
        return (T) ((value != null && value instanceof Binary) ?
                serializer.deserialize(((Binary) value).getData(), classLoader) : null);
    }
}
