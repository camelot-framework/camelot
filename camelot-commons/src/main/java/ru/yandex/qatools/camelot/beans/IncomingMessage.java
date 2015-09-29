package ru.yandex.qatools.camelot.beans;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class IncomingMessage<T extends Serializable> implements Serializable {
    String headerName;
    T headerValue;
    String inputUri;
    T message;

    public IncomingMessage(String inputUri, T message) {
        this.inputUri = inputUri;
        this.message = message;
    }

    public IncomingMessage(String inputUri, T message, String headerName, T headerValue) {
        this(inputUri, message);
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    public String getInputUri() {
        return inputUri;
    }

    public T getMessage() {
        return message;
    }

    public String getHeaderName() {
        return headerName;
    }

    public T getHeaderValue() {
        return headerValue;
    }
}
