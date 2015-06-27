package ru.yandex.qatools.camelot.api.error;

/**
 * @author Ilya Sadykov
 */
public class RepositoryUnreachableException extends RuntimeException {
    public RepositoryUnreachableException(Throwable cause) {
        super(cause);
    }

    public RepositoryUnreachableException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryUnreachableException(String message) {
        super(message);
    }
}
