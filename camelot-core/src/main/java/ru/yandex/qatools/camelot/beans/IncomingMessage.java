package ru.yandex.qatools.camelot.beans;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class IncomingMessage implements Serializable {
    String headerName;
    Object headerValue;
    String inputUri;
    Object message;

    public IncomingMessage(String inputUri, Object message) {
        this.inputUri = inputUri;
        this.message = message;
    }

    public IncomingMessage(String inputUri, Object message, String headerName, Object headerValue) {
        this(inputUri, message);
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    public String getInputUri() {
        return inputUri;
    }

    public Object getMessage() {
        return message;
    }

    public String getHeaderName() {
        return headerName;
    }

    public Object getHeaderValue() {
        return headerValue;
    }
}
