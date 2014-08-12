package ru.yandex.qatools.camelot.core.builders;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static ru.yandex.qatools.camelot.core.builders.ClassMetadata.get;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class ClassMetadataTest {

    class TestClass {

        @Deprecated
        public void annotatedMethod() {

        }
    }

    @Test
    public void testCache() {
        assertSame("Should return same instances of ClassInfo", get(TestClass.class), get(TestClass.class));
        assertSame("Should return same instances of Method[]",
                get(TestClass.class).getAnnotatedMethods(Deprecated.class),
                get(TestClass.class).getAnnotatedMethods(Deprecated.class));
    }
}
