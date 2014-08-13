package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Body;
import org.apache.camel.Headers;
import org.slf4j.Logger;
import ru.yandex.qatools.camelot.core.beans.TestEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.api.Constants.Headers.LABEL;
import static ru.yandex.qatools.camelot.util.CloneUtil.deepCopy;
import static ru.yandex.qatools.camelot.util.SerializeUtil.checkAndGetBytesInput;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class Splitter {
    public static final String BY_LABEL = "byLabel";
    private final Logger logger = getLogger(getClass());

    public List<Object> byLabel(@Body Object input, @Headers Map<String, Object> headers) {
        List<Object> res = new ArrayList<>();
        try {
            TestEvent testEvent = checkAndGetBytesInput(TestEvent.class, input, getClass().getClassLoader());
            if (testEvent != null && testEvent.getConfig() != null) {
                for (String label : testEvent.getConfig().getLabels()) {
                    try {
                        final TestEvent newEvent = (TestEvent) deepCopy(testEvent);
                        newEvent.getConfig().getLabels().clear();
                        newEvent.getConfig().getLabels().add(label);
                        res.add(newEvent);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                final String headerLabel = (String) headers.get(LABEL);
                if (!isBlank(headerLabel)) {
                    headers.put(CORRELATION_KEY, headerLabel);
                    res.add(input);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to split: ", e);
            res.add(input);
        }
        return res;
    }
}
