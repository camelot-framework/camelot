package ru.yandex.qatools.camelot.mongodb.test;

import ru.yandex.qatools.camelot.api.annotations.Processor;

/**
 * @author Ilya Sadykov
 */
public class BroadcastProcessor {

    @Processor
    public void onString(String message) {
    }
}
