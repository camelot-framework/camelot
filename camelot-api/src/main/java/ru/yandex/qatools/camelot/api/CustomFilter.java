package ru.yandex.qatools.camelot.api;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface CustomFilter {

    boolean filter(Object body);
}
