package ru.yandex.qatools.camelot.error;

/**
 * Being thrown when processing cannot be dispatched
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class DispatchException extends Exception {
    public DispatchException(String message) {
        super(message);
    }

    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public DispatchException(Throwable cause) {
        super(cause);
    }

}
