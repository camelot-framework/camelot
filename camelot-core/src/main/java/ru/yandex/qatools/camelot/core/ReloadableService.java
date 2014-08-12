package ru.yandex.qatools.camelot.core;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ReloadableService {

    /**
     * Initialize service
     */
    void init();

    /**
     * Reload state and start service classes
     */
    void reloadAndStart();

    /**
     * Stop service classes, reload and start them again
     */
    void reload();

    /**
     * Stop service classes
     */
    void stop();

    /**
     * Indicates that service is currently reloading its context
     */
    boolean isLoading();

}
