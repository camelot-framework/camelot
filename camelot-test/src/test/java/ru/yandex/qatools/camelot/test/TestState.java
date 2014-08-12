package ru.yandex.qatools.camelot.test;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestState implements Serializable {
    private String message;

    public TestState() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
