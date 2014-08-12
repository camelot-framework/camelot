package ru.yandex.qatools.camelot.core.beans;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class InitEvent implements Serializable {
    private String label;

    public InitEvent(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
