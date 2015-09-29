package ru.yandex.qatools.camelot.error;

/**
 * Thrown when there's some metadata reading error
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class MetadataException extends RuntimeException {
    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}
