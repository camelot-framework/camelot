package ru.yandex.qatools.camelot.core.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class TestEventDescriptionImpl implements TestEventDescription {

    private final TestEventData eventData;

    private final FailureDescription failureInfo;

    private final List<MetaData> metas;

    private final Notification testResult;

    public TestEventDescriptionImpl(TestEventData eventData, Notification testResult) {
        this(eventData, testResult, null);
    }

    public TestEventDescriptionImpl(TestEventData eventData, Notification testResult, FailureDescription failureInfo) {
        this(eventData, testResult, failureInfo, null);
    }

    public TestEventDescriptionImpl(TestEventData eventData, Notification testResult, FailureDescription failureInfo,
                                    List<Meta> metas) {
        this.eventData = eventData;
        this.failureInfo = failureInfo;
        this.testResult = testResult;
        this.metas = new ArrayList<MetaData>();
        if (metas != null) {
            for (Meta meta : metas) {
                this.metas.add(meta);
            }
        }
    }

    @Override
    public TestEventData getEventData() {
        return eventData;
    }

    @Override
    public FailureDescription getFailureInfo() {
        return failureInfo;
    }

    @Override
    public List<MetaData> getMetas() {
        return metas;
    }

    @Override
    public Notification getTestResult() {
        return testResult;
    }
}
