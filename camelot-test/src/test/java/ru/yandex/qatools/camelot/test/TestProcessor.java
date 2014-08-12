package ru.yandex.qatools.camelot.test;

import ru.yandex.qatools.camelot.api.annotations.ConfigValue;
import ru.yandex.qatools.camelot.api.annotations.Processor;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestProcessor {
    @ConfigValue("camelot-test.someTestingProperty")
    String property = "";

    @Processor
    public String onNodeEvent(String event) {
        return event + "processed" + property;
    }

    @Processor
    public Object fallback(Object event) {
        return event;
    }
}
