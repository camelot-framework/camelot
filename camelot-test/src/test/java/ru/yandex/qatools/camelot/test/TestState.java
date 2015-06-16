package ru.yandex.qatools.camelot.test;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class TestState implements Serializable {

    private String message;

    private boolean cronFlag;

    public TestState() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCronFlagSet() {
        return cronFlag;
    }

    public void setCronFlag(boolean cronFlag) {
        this.cronFlag = cronFlag;
    }
}
