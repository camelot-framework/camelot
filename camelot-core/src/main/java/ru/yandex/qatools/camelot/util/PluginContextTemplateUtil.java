package ru.yandex.qatools.camelot.util;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginContextTemplateUtil {
    public static final String CSS_URL_EXPR = ":(:?[^\\nu]*)url\\([\"']?([^\"'\\)]*)['\"]?\\)";
    public static final String CSS_IMPORT_EXPR = "@import(:?[^\"'u]*)(?:url\\()?[\"']?([^\"\\)]+)[\"']?\\)?";

    public static String replaceCss(String text, String prefix) {
        return text.
                replaceAll(CSS_URL_EXPR, ":$1url(\"" + prefix + "/$2\")").
                replaceAll(CSS_IMPORT_EXPR, "@import$1url(\"" + prefix + "/$2\")");
    }
}
