package ru.yandex.qatools.camelot.error;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginsSystemException extends RuntimeException {
    public PluginsSystemException(String message) {
        super(message);
    }

    public PluginsSystemException(Throwable cause) {
        super(cause);
    }

    public PluginsSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
