package ru.yandex.qatools.camelot.core.beans;

import java.io.Serializable;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CounterState implements Serializable {
    public String label = "";
    public String label2 = "";
    public int count = 0;
    public int count2 = 0;
}
