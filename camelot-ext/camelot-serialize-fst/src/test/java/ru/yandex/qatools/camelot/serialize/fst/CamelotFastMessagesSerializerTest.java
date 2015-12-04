package ru.yandex.qatools.camelot.serialize.fst;

import org.junit.Test;

import static java.lang.Thread.currentThread;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov
 */
public class CamelotFastMessagesSerializerTest {

    public static final ClassLoader cl = currentThread().getContextClassLoader();
    private CamelotFastMessagesSerializer serializer = new CamelotFastMessagesSerializer();

    @Test
    public void testSerialize() throws Exception {
        assertThat(serializer.serialize(null, cl), nullValue());
        assertThat(serializer.serialize(null, cl), nullValue());
        assertThat(serializer.serialize(new Object(), cl), notNullValue());
        verifyThereAndBack("hello");
        verifyThereAndBack(95L);
        verifyThereAndBack(map("hello", "world"));
    }

    private void verifyThereAndBack(Object object) {
        assertThat(serializer.deserialize(serializer.serialize(object, cl), cl), is(object));
    }

}