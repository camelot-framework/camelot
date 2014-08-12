package ru.yandex.qatools.camelot.core.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CollectEventsState implements Serializable {
    public List<Object> collected = new ArrayList<>();
}
