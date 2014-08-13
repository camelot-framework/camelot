package ru.yandex.qatools.camelot.core.beans;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public abstract class TestState implements Serializable {

    TestEvent event;

    public TestEvent getEvent() {
        return event;
    }

    public void setEvent(TestEvent event) {
        this.event = event;
    }
}
