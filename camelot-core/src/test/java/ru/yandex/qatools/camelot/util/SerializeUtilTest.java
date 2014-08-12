package ru.yandex.qatools.camelot.util;

import org.junit.Test;
import ru.yandex.qa.beans.TestStarted;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static ru.yandex.qatools.beans.test.TestEventGenerator.createTestStarted;
import static ru.yandex.qatools.camelot.util.SerializeUtil.deserializeFromBytes;
import static ru.yandex.qatools.camelot.util.SerializeUtil.serializeToBytes;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class SerializeUtilTest {

    @Test
    public void testSerialize() {
        assertNull(serializeToBytes(null));
    }

    @Test
    public void testFastSerializerDeserializer() throws Exception {
        final TestStarted oldObject = createTestStarted();
        byte[] bytes = serializeToBytes(oldObject);

        TestStarted newObject = deserializeFromBytes(bytes, TestStarted.class);
        assertNotNull(newObject);
        assertThat(newObject.getClassname(), is(oldObject.getClassname()));
    }
}

