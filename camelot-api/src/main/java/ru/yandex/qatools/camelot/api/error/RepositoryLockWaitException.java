package ru.yandex.qatools.camelot.api.error;

/**
 * @author Ilya Sadykov
 */
public class RepositoryLockWaitException extends RuntimeException {
    public RepositoryLockWaitException(Throwable cause) {
        super(cause);
    }

    public RepositoryLockWaitException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryLockWaitException(String message) {
        super(message);
    }
}
