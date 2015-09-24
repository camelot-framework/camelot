package ru.yandex.qatools.camelot.error;

/**
 * Being thrown when processing cannot be dispatched
 *
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CallException extends Exception {
    public CallException(String message) {
        super(message);
    }

    public CallException(Throwable cause) {
        super(cause);
    }
}
