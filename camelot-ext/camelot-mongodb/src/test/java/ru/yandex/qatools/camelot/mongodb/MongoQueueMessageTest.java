package ru.yandex.qatools.camelot.mongodb;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.common.BasicMessagesSerializer;
import ru.yandex.qatools.camelot.common.MessagesSerializer;
import ru.yandex.qatools.camelot.test.CamelotTestRunner;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author Ilya Sadykov
 */
@RunWith(CamelotTestRunner.class)
public class MongoQueueMessageTest {

    @Test
    public void testSerializeDeserializeMongoMessage() throws Exception {
        final MessagesSerializer serializer = new BasicMessagesSerializer();
        final ClassLoader cl = getClass().getClassLoader();
        final MongoQueueMessage msg = new MongoQueueMessage("pluginId", "message", "topic");
        assertThat(serializer.deserialize(serializer.serialize(msg, cl), cl),
                instanceOf(MongoQueueMessage.class));
    }

    @Test(expected = RuntimeException.class)
    public void testNotSerializableProvided() throws Exception {
        new MongoQueueMessage("aaa", new Object(), "topic");
    }
}