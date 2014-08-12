package ru.yandex.qatools.camelot.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.camelot.util.EnumUtil.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class EnumUtilTest {
    public static enum TestEnum {
        first,
        second
    }

    @Test
    public void testFromOrdinal() {
        assertEquals(TestEnum.first, fromOrdinal(TestEnum.class, 0));
        assertEquals(TestEnum.second, fromOrdinal(TestEnum.class, 1));
    }

    @Test
    public void testFromString() {
        assertEquals(TestEnum.first, fromString(TestEnum.class, "first"));
    }

    @Test
    public void testRandom() {
        for (int i = 0; i < 100; ++i) {
            assertTrue(enumContains(TestEnum.class, random(TestEnum.class).name()));
        }
    }
}
