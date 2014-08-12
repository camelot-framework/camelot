package ru.yandex.qatools.camelot.core;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface RoutingService {
    /**
     * Returns the input endpoint uri
     */
    String getInputUri();

    /**
     * Returns the output endpoint uri
     */
    String getOutputUri();

}
