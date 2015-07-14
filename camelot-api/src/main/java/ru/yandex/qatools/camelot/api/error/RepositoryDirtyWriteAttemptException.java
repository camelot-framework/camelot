package ru.yandex.qatools.camelot.api.error;

/**
 * @author Ilya Sadykov
 */
public class RepositoryDirtyWriteAttemptException extends RuntimeException {
    public RepositoryDirtyWriteAttemptException(String message) {
        super(message);
    }

    public RepositoryDirtyWriteAttemptException(String message, Throwable cause) {
        super(message, cause);
    }
}
