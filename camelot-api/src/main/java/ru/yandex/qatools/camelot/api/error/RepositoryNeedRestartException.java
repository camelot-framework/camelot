package ru.yandex.qatools.camelot.api.error;

/**
 * @author Ilya Sadykov
 */
public class RepositoryNeedRestartException extends RuntimeException {
    public RepositoryNeedRestartException(Throwable cause) {
        super(cause);
    }

    public RepositoryNeedRestartException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryNeedRestartException(String message) {
        super(message);
    }
}
