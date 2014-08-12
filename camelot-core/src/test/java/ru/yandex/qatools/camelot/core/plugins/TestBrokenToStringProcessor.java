package ru.yandex.qatools.camelot.core.plugins;

import ru.yandex.qa.beans.TestBroken;
import ru.yandex.qatools.camelot.api.annotations.Filter;
import ru.yandex.qatools.camelot.api.annotations.Processor;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@Filter(instanceOf = TestBroken.class)
public class TestBrokenToStringProcessor {

    @Processor
    public String process(TestBroken event) {
        return event.getClass().getName();
    }

}
