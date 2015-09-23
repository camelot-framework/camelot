package ru.yandex.qatools.camelot.util;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.annotations.MainInput;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static ru.yandex.qatools.camelot.util.ServiceUtil.injectAnnotatedField;
import static ru.yandex.qatools.camelot.util.ServiceUtil.setFieldValue;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ServiceUtilTest {

    SomeClass someObject;

    @Before
    public void init() {
        someObject = new SomeClass();
    }

    @Test
    public void testInjectAnnotatedField() throws Exception {
        injectAnnotatedField(SomeClass.class, someObject, SomeAnnotation.class, "value");
        injectAnnotatedField(SomeClass.class, someObject, SomeAnnotation.class, new Object());
        assertEquals(someObject.someField, "value");
    }

    @Test
    public void testSetFieldValue() throws Exception {
        final Field someField = SomeClass.class.getDeclaredField("someField");
        setFieldValue(someField, someObject, "test");
        setFieldValue(someField, null, "test");
        assertEquals(someObject.someField, "test");
    }


    @Retention(RUNTIME)
    @Target({FIELD})
    static @interface SomeAnnotation {
    }

    static class SomeClass {
        @SomeAnnotation
        String someField;

        @MainInput
        EventProducer input;
    }
}
