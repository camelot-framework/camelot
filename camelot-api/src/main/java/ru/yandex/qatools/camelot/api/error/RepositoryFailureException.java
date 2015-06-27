package ru.yandex.qatools.camelot.api.error;

/**
 * @author Ilya Sadykov
 */
public class RepositoryFailureException extends RuntimeException {
    public RepositoryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
