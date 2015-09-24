package ru.yandex.qatools.camelot.common;

import org.junit.Test;
import ru.yandex.qatools.camelot.api.annotations.Processor;
import ru.yandex.qatools.camelot.common.MetadataClassInfo;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static ru.yandex.qatools.camelot.common.Metadata.getMeta;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MetadataTest {

    public static class TestClass {

        @Processor
        public void process() {

        }

        @Processor
        public void anotherMethod(String param1, Integer param2) {

        }

        @Processor
        public void anotherMethod(Integer param1, String param2) {

        }

        @Processor
        public void thirdMethod(Integer param1) {

        }
    }

    @Test
    public void testMetadata() {
        final MetadataClassInfo meta = getMeta(TestClass.class);
        assertSame(meta, getMeta(TestClass.class));
        assertThat(meta.getAnnotatedMethods(Processor.class).length, is(4));
        assertThat(meta.getMethodsByParamTypes(Processor.class, Integer.class).size(), is(1));
        assertThat(meta.getMethodsByParamTypes(Processor.class, String.class, Integer.class).size(), is(2));
        assertThat(meta.getMethodsByParamTypes(Processor.class, Float.class).isEmpty(), is(true));
    }
}
