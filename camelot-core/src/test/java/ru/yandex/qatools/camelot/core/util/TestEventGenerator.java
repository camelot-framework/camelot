package ru.yandex.qatools.camelot.core.util;

import ru.yandex.qatools.camelot.core.beans.Config;
import ru.yandex.qatools.camelot.core.beans.EmailConfig;
import ru.yandex.qatools.camelot.core.beans.FailConfig;
import ru.yandex.qatools.camelot.core.beans.Failure;
import ru.yandex.qatools.camelot.core.beans.TestBroken;
import ru.yandex.qatools.camelot.core.beans.TestEvent;
import ru.yandex.qatools.camelot.core.beans.TestFailed;
import ru.yandex.qatools.camelot.core.beans.TestFailure;
import ru.yandex.qatools.camelot.core.beans.TestPassed;
import ru.yandex.qatools.camelot.core.beans.TestSkipped;
import ru.yandex.qatools.camelot.core.beans.TestStarted;

import java.util.Arrays;

import static ru.yandex.qatools.camelot.core.util.TestEventsUtils.copyOf;

public class TestEventGenerator {

    public static TestStarted createTestStarted() {
        return copyOf(createTestEvent(), TestStarted.class);
    }

    public static TestStarted createTestStarted(String postfix, String... labels) {
        return copyOf(createTestEvent(postfix, labels), TestStarted.class);
    }

    public static TestPassed createTestPassed() {
        return copyOf(createTestEvent(), TestPassed.class);
    }

    public static TestPassed createTestPassed(String postfix, String... labels) {
        return copyOf(createTestEvent(postfix, labels), TestPassed.class);
    }

    public static TestFailed createTestFailed() {
        return createTestFailed(createFailure());
    }

    public static TestFailed createTestFailed(String postfix, String... labels) {
        return createTestFailed(createFailure(), postfix, labels);
    }

    public static TestFailed createTestFailed(Failure failure) {
        return createTestFailure(failure, TestFailed.class);
    }

    public static TestFailed createTestFailed(Failure failure, String postfix, String... labels) {
        return createTestFailure(failure, TestFailed.class, postfix, labels);
    }

    public static TestBroken createTestBroken() {
        return createTestBroken(createFailure());
    }

    public static TestBroken createTestBroken(String postfix, String... labels) {
        return createTestBroken(createFailure(), postfix, labels);
    }

    public static TestBroken createTestBroken(Failure failure) {
        return createTestFailure(failure, TestBroken.class);
    }

    public static TestBroken createTestBroken(Failure failure, String postfix, String... labels) {
        return createTestFailure(failure, TestBroken.class, postfix, labels);
    }

    public static TestSkipped createTestSkipped() {
        return createTestSkipped(createFailure());
    }

    public static TestSkipped createTestSkipped(String postfix) {
        return createTestSkipped(createFailure(), postfix);
    }

    public static TestSkipped createTestSkipped(Failure failure) {
        return createTestFailure(failure, TestSkipped.class);
    }

    public static TestSkipped createTestSkipped(Failure failure, String postfix) {
        return createTestFailure(failure, TestSkipped.class, postfix);
    }

    public static TestSkipped createTestSkippedWithProfile(String mod) {
        TestSkipped o = createTestFailure(createFailure(), TestSkipped.class);
        o.setProfile(o.getProfile() + mod);
        return o;
    }

    public static <T extends TestFailure> T createTestFailure(Failure failure, Class<T> clazz, String postfix, String... labels) {
        T message = copyOf(createTestEvent(postfix, labels), clazz);
        message.setFailureDetail(failure);
        return message;
    }

    public static <T extends TestFailure> T createTestFailure(Failure failure, Class<T> clazz) {
        return createTestFailure(failure, clazz, "");
    }

    public static TestEvent createTestEvent() {
        return createTestEvent(failConfig(1));
    }

    public static TestEvent createTestEvent(String[] labels) {
        return createTestEvent(failConfig(1, labels));
    }

    public static TestEvent createTestEvent(final String postfix, final String... labels) {
        return createTestEvent(failConfig(1, labels), postfix);
    }

    public static TestEvent createTestEvent(final Config nested) {
        return createTestEvent(nested, "");
    }

    public static TestEvent createTestEvent(final Config nested, final String postfix) {
        return new TestEvent() {
            {
                setClassname("test_classname" + postfix);
                setPackagename("test_package" + postfix);
                setProfile("test_profile" + postfix);
                setMethodname("test_methodname" + postfix);
                setSystem("unit_test" + postfix);
                setTime(System.currentTimeMillis());
                setTimestamp(System.currentTimeMillis());
                setConfig(nested);
            }
        };
    }

    public static Failure createFailure() {
        return new Failure() {
            {
                setMessage("test_failure_message");
                setStackTrace("test_failure_stacktrace");
                setType("test_failure_type");
            }
        };
    }

    public static Config failConfig(final int num, final String... lbls) {
        return new Config() {
            {
                getConfigs().add(
                        new FailConfig() {{
                            setNumFailures(num);
                        }}
                );
                getLabels().addAll(Arrays.asList(lbls));
            }
        };
    }

    public static Config emailConfig() {
        return new Config() {
            {
                getConfigs().add(
                        new EmailConfig()
                );
            }
        };
    }

}
