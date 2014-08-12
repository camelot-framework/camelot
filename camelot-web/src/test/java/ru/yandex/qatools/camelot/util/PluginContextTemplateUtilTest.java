package ru.yandex.qatools.camelot.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static ru.yandex.qatools.camelot.util.PluginContextTemplateUtil.replaceCss;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginContextTemplateUtilTest {

    private static final String cssText =
            "@import \"style1.css\" some values1;\n" +
                    "@import url(style2.css) some values2;\n" +
                    "@import url(\"style3.css\") some values3;\n" +
                    ".class1, tag1 {\n" +
                    "  some-property1: some values4 url(image1.gif) some values5;\n" +
                    "}\n" +
                    ".class2 {\n" +
                    "  some-property2: some values6 url('image2.png') some values7;\n" +
                    "}";

    @Test
    public void testCssReplace() {
        final String res = replaceCss(cssText, "http://localhost:8080/camelot/plugin/pluginId");
        assertTrue(res.contains("@import url(\"http://localhost:8080/camelot/plugin/pluginId/style1.css\") some values1"));
        assertTrue(res.contains("@import url(\"http://localhost:8080/camelot/plugin/pluginId/style2.css\") some values2;"));
        assertTrue(res.contains("@import url(\"http://localhost:8080/camelot/plugin/pluginId/style3.css\") some values3;"));
        assertTrue(res.contains("some-property1: some values4 url(image1.gif) some values5;"));
        assertTrue(res.contains("some-property2: some values6 url('image2.png') some values7;"));
    }
}
