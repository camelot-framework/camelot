package ru.yandex.qatools.camelot.util;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.camelot.api.EventProducer;
import ru.yandex.qatools.camelot.api.PluginEndpoints;
import ru.yandex.qatools.camelot.api.annotations.MainInput;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.yandex.qatools.camelot.Constants.TMP_INPUT_BUFFER_URI;
import static ru.yandex.qatools.camelot.util.ServiceUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ServiceUtilTest {

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

    @Test
    public void testInjectTmpInputBufferProducer() throws Exception {
        PluginEndpoints endpoints = mock(PluginEndpoints.class);
        CamelContext camelContext = mock(CamelContext.class);
        when(endpoints.getMainInputUri()).thenReturn("some-url");
        when(camelContext.createProducerTemplate()).thenReturn(mock(ProducerTemplate.class));
        injectTmpInputBufferProducers(endpoints, someObject, SomeClass.class, camelContext, TMP_INPUT_BUFFER_URI);
        assertNotNull(someObject.input);
    }
}
