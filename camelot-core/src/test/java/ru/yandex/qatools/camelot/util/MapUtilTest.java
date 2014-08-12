package ru.yandex.qatools.camelot.util;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ru.yandex.qatools.camelot.util.MapUtil.map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MapUtilTest {


    @Test
    public void testMapUtil() {
        Map<String, Object> map = map(
                "long", (Object) 1L,
                "map", map("float", Float.valueOf(1.0f))
        );

        assertEquals(1L, map.get("long"));
        assertEquals(1.0f, ((Map) map.get("map")).get("float"));
    }
}
