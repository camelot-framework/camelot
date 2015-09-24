package ru.yandex.qatools.camelot.util;


import ru.yandex.qatools.camelot.core.beans.Config;
import ru.yandex.qatools.camelot.core.beans.TestBroken;
import ru.yandex.qatools.camelot.core.beans.TestDropped;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.camelot.core.beans.TestFailed;
import ru.yandex.qatools.camelot.core.beans.TestFailure;
import ru.yandex.qatools.camelot.core.beans.TestPassed;
import ru.yandex.qatools.camelot.core.beans.TestSkipped;
import ru.yandex.qatools.camelot.core.beans.TestStarted;

public final class TestEventsUtils {

    public static TestStarted copyOf(TestStarted event) {
        return copyOf(event, TestStarted.class);
    }

    public static TestPassed copyOf(TestPassed event) {
        return copyOf(event, TestPassed.class);
    }

    public static TestDropped copyOf(TestDropped event) {
        return copyOf(event, TestDropped.class);
    }

    public static TestFailed copyOf(TestFailed event) {
        return copyOf(event, TestFailed.class);
    }

    public static TestBroken copyOf(TestBroken event) {
        return copyOf(event, TestBroken.class);
    }

    public static TestSkipped copyOf(TestSkipped event) {
        return copyOf(event, TestSkipped.class);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TestEvent> T msgFrom(TestEvent event) {
        return (T) copyOf(event, event.getClass());
    }

    public static <T extends TestEvent> T copyOf(TestEvent event, Class<T> clazz) {
        T result;
        try {
            result = clazz.newInstance();
            Config config = new Config();
            config.getLabels().addAll(event.getConfig().getLabels());
            config.getConfigs().addAll(event.getConfig().getConfigs());
            result.setConfig(config);
            result.setSystem(event.getSystem());
            result.setProfile(event.getProfile());
            result.setPackagename(event.getPackagename());
            result.setClassname(event.getClassname());
            result.setMethodname(event.getMethodname());
            result.setTime(event.getTime());
            result.setTimestamp(event.getTimestamp());

            if (result instanceof TestFailure && event instanceof TestFailure) {
                ((TestFailure) result).setFailureDetail(((TestFailure) event).getFailureDetail());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
