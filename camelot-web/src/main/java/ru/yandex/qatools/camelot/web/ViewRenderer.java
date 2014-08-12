package ru.yandex.qatools.camelot.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ViewRenderer {
    String render(String layoutFileName, InputStream layout, String viewFileName, InputStream template) throws IOException;

    String render(String layoutName, String viewName) throws IOException;

    String render(String layoutName, String viewName, Map<String, Object> attrs) throws IOException;

    String render(String viewFileName) throws IOException;

    String render(String viewFileName, Map<String, Object> attrs) throws IOException;

    String renderWithDefaultLayout(Class source, Map<String, Object> attrs) throws IOException;

    <T> String renderWithDefaultLayout(Class<T> source) throws IOException;

    String renderWithDefaultLayout(String viewFileName, Map<String, Object> attrs) throws IOException;

    String render(String layoutFileName, InputStream layout, String viewFileName,
                  InputStream view, Map<String, Object> attrs) throws IOException;

    String renderWithDefaultLayout(String viewFileName) throws IOException;
}
