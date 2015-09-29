package ru.yandex.qatools.camelot.util;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.TestStarted;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static ru.yandex.qatools.camelot.util.TestEventGenerator.createTestStarted;
import static ru.yandex.qatools.camelot.util.SerializeUtil.*;

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


    @Test
    public void testWrapUnwrapBodyWithMeta() throws Exception {
        final StopEvent event = new StopEvent();
        event.setMethodname("someMethod");
        final ClassLoader cl = getClass().getClassLoader();
        final byte[] serialized = serializeToBytes(event, cl);
        final byte[] wrapped = wrapBytesWithMeta(serialized, event.getClass().getName());
        final byte[] unwrapped = unwrapBytesWithMeta(wrapped);
        final String bodyClass = unwrapBodyClassName(wrapped);
        assertThat(bodyClass, equalTo(event.getClass().getName()));
        assertThat(unwrapped, equalTo(serialized));
        final Object deserialized = deserializeFromBytes(unwrapped, cl);
        assertThat(deserialized, instanceOf(event.getClass()));
        assertThat(((StopEvent) deserialized).getMethodname(), equalTo(event.getMethodname()));
    }

    @Test
    public void testToStringFromString() throws Exception {
        final StopEvent event = new StopEvent();
        event.setClassname("classname");
        final byte[] serialized = wrapBytesWithMeta(serializeToBytes(event), event.getClass().getName());
        final String result = printBase64Binary(serialized);
        final byte[] inputBytes = parseBase64Binary(new String(result.getBytes(UTF_8)));
        final StopEvent deserialized = deserializeFromBytes(unwrapBytesWithMeta(inputBytes), StopEvent.class);
        assertThat(deserialized.getClassname(), Matchers.equalTo(event.getClassname()));
    }
}

