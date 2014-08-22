package ru.yandex.qatools.camelot;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.api.Storage;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.beans.*;
import ru.yandex.qatools.camelot.core.plugins.ByCustomHeaderAggregator;
import ru.yandex.qatools.camelot.core.plugins.WithoutIdAggregator;

import static java.lang.Thread.sleep;
import static java.util.Calendar.HOUR_OF_DAY;
import static org.junit.Assert.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.CORRELATION_KEY;
import static ru.yandex.qatools.camelot.core.util.TestEventGenerator.*;
import static ru.yandex.qatools.camelot.core.util.TestEventsUtils.copyOf;
import static ru.yandex.qatools.camelot.util.DateUtil.calThen;
import static ru.yandex.qatools.camelot.util.DateUtil.hourAgo;
import static ru.yandex.qatools.camelot.util.SerializeUtil.checkAndGetBytesInput;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:camelot-core-context.xml",
        "classpath*:test-camelot-core-context.xml"
})
@DirtiesContext(classMode = AFTER_CLASS)
@MockEndpoints("*")
public class AggregatorsTest extends BasicAggregatorsTest {

    @Autowired
    ProcessingEngine processingEngine;

    @Test
    public void testWithoutIdAggregator() throws Exception {
        endpointWithoutIdOutput.reset();
        endpointWithoutIdOutput.expectedMessageCount(1);

        sendEvent(WithoutIdAggregator.class, new InitEvent("test"));
        endpointWithoutIdOutput.assertIsSatisfied(3000);

        expectExchangeExists(endpointInitializableOutput,
                "Must receive counter with 1",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null && first.count == 1;
                    }
                });
    }

    @Test
    public void testInitializableAggregator() throws Exception {
        endpointInitializableOutput.expectedMessageCount(1);
        endpointInitializableOutput.assertIsSatisfied(3000);

        expectExchangeExists(endpointInitializableOutput,
                "Must receive counter with 1",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(
                                CounterState.class,
                                exchange.getIn().getBody(),
                                classLoader);
                        return first != null
                                && first.count == 1
                                && first.label.equals("test");
                    }
                });
    }

    @Test
    public void testTestStartedCounterAggregator() throws Exception {
        endpointTestStartedOutput.reset();
        endpointTestStartedOutput.expectedMessageCount(1);

        String uuid1 = uuid();
        String uuid2 = uuid();
        TestStarted testStarted = createTestStarted(uuid1);
        sendTestEvent("test-started", testStarted, uuid1);
        sendTestEvent("test-started", testStarted, uuid2);
        sendStopEvent("test-started", copyOf(testStarted, StopTestStartedCounter.class));

        endpointTestStartedOutput.assertIsSatisfied(2000);

        expectExchangeExists(endpointTestStartedOutput,
                "Must receive counter with 2",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        Object first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null && ((CounterState) first).count == 2;
                    }
                });
    }

    @Test
    public void testByMethodAggregator() throws Exception {
        endpointByMethodOutput.reset();
        endpointByMethodOutput.expectedMessageCount(1);

        String uuid1 = uuid();
        String uuid2 = uuid();
        sendTestEvent("by-method", createTestStarted(uuid1), uuid1);
        sendTestEvent("by-method", createTestFailed(uuid1), uuid1);
        sendTestEvent("by-method", createTestStarted(uuid1), uuid2);
        sendTestEvent("by-method", createTestPassed(uuid1), uuid2);

        endpointByMethodOutput.assertIsSatisfied(2000);

        expectExchangeExists(endpointByMethodOutput,
                "TestPassedState must exist!",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        TestPassedState first = checkAndGetBytesInput(TestPassedState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null;
                    }
                });
    }

    @Test
    public void testExpressionAggregator() throws InterruptedException {
        endpointByHourOfDayOutput.reset();
        endpointByHourOfDayOutput.expectedMinimumMessageCount(2);

        String testName = "test";

        TestStarted oldTestStarted = createTestStarted(testName);
        TestBroken oldTestBroken = createTestBroken(testName);
        oldTestStarted.setTime(hourAgo().getTime());
        oldTestBroken.setTime(hourAgo().getTime());
        TestStarted newTestStarted = createTestStarted(testName);
        TestBroken newTestBroken = createTestBroken(testName);
        String uuid1 = uuid();
        String uuid2 = uuid();

        sendTestEvent("lifecycle", oldTestStarted, uuid1);
        sendTestEvent("lifecycle", oldTestBroken, uuid1);
        sendTestEvent("lifecycle", newTestStarted, uuid2);
        sendTestEvent("lifecycle", newTestBroken, uuid2);

        sendStopEvent("lifecycle", copyOf(oldTestStarted, StopByHourOfDay.class));
        sendStopEvent("lifecycle", copyOf(newTestStarted, StopByHourOfDay.class));

        endpointByHourOfDayOutput.assertIsSatisfied(2000);

        endpointByHourOfDayOutput.expectedHeaderReceived(CORRELATION_KEY,
                calThen(oldTestBroken.getTimestamp()).get(HOUR_OF_DAY));
        endpointByHourOfDayOutput.expectedHeaderReceived(CORRELATION_KEY,
                calThen(newTestBroken.getTimestamp()).get(HOUR_OF_DAY));

        expectExchangeExists(endpointByHourOfDayOutput,
                "Must receive one of the events with count 1!",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null && first.count == 1;
                    }
                });
    }

    @Test
    public void testByLabelBrokenAggregator() throws Exception {
        endpointBrokenByLabelOutput.reset();
        endpointBrokenByLabelOutput.expectedMessageCount(2);

        String[] labels = new String[]{"bylabel-label1", "bylabel-label2"};
        String uuid = uuid();
        for (int i = 0; i < 2; ++i) {
            sendTestEvent("broken-by-label", createTestBroken(uuid, labels), uuid());
        }
        sendStopEvent("broken-by-label",
                copyOf(createTestStarted(uuid, labels),
                        StopByLabelBroken.class));

        endpointBrokenByLabelOutput.assertIsSatisfied(4000);

        for (final String label : labels) {
            expectExchangeExists(endpointBrokenByLabelOutput,
                    label + " exchange must exist!", new Predicate() {
                        @Override
                        public boolean matches(Exchange exchange) {
                            CounterState obj = checkAndGetBytesInput(CounterState.class,
                                    exchange.getIn().getBody(), classLoader);
                            return obj != null && obj.count > 1 && obj.label.equals(label);
                        }
                    });
        }
    }

    @Test
    public void testAllSkippedAggregator() throws Exception {
        endpointAllSkippedOutput.reset();
        endpointAllSkippedOutput.expectedMessageCount(2);

        String uuid = uuid();
        for (int i = 0; i < 4; ++i) {
            sendTestEvent("all-skipped", createTestSkipped(uuid), uuid + i);
            if (i % 2 == 1) {
                sendStopEvent("all-skipped",
                        copyOf(createTestStarted(uuid),
                                StopAllSkipped.class));
            }
        }

        endpointAllSkippedOutput.assertIsSatisfied(4000);
        expectExchangeExists(endpointAllSkippedOutput,
                "First counter must have 2",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null && first.count == 2;
                    }
                });
    }

    @Test
    public void testEventsInDifferentOrder() throws Exception {
        endpointLifecycleOutput.reset();
        endpointLifecycleOutput.expectedMinimumMessageCount(2);

        String uuid1 = uuid();
        String uuid2 = uuid();
        sendTestEvent("lifecycle", createTestStarted(uuid1), uuid1);
        sendTestEvent("lifecycle", createTestFailed(uuid1), uuid1);
        sendTestEvent("lifecycle", createTestFailed(uuid1), uuid2);
        sendTestEvent("lifecycle", createTestStarted(uuid1), uuid2);

        endpointLifecycleOutput.assertIsSatisfied(2000);
        TestEvent first = checkAndGetBytesInput(TestFailed.class,
                endpointLifecycleOutput.getExchanges().get(0).getIn().getBody(),
                classLoader);
        TestEvent second = checkAndGetBytesInput(TestFailed.class,
                endpointLifecycleOutput.getExchanges().get(1).getIn().getBody(),
                classLoader);


        assertNotNull("Must contain timestamp", first.getTimestamp());
        assertNotNull("Must contain timestamp", second.getTimestamp());
    }


    @Test
    public void testBrokenToStringProcessor() throws Exception {
        endpointEventToStringOutput.reset();
        endpointEventToStringOutput.expectedMessageCount(1);
        String uuid = uuid();
        sendTestEvent("lifecycle", createTestStarted(uuid), uuid);
        sendTestEvent("lifecycle", createTestBroken(uuid), uuid);
        endpointEventToStringOutput.assertIsSatisfied(2000);
        expectExchangeExists(endpointEventToStringOutput,
                "Output must be string of class name",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        String first = checkAndGetBytesInput(String.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null && TestBroken.class.getName().equals(first);
                    }
                });
    }

    @Test
    public void testAggregatorWithTimer() throws Exception {
        endpointWithTimer.reset();
        endpointWithTimer.expectedMessageCount(1);
        String uuid1 = uuid();
        sendTestEvent("with-timer", createTestStarted(), uuid1);
        sendTestEvent("with-timer", createTestStarted(), uuid1);
        sleep(3000); // wait for 3 seconds
        sendTestEvent("with-timer", new StopAggregatorWithTimer(), uuid1);
        endpointWithTimer.assertIsSatisfied(2000);
        expectExchangeExists(endpointWithTimer,
                "Output must contain 2 < count1 <= 10 && 1 <= count2 < 5",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        assertNotNull("First must not be null", first);
                        boolean res = first.count2 >= 1
                                && first.count2 <= 10
                                && first.count >= 1
                                && first.count <= 5;
                        assertTrue(String.format(
                                "Expect 1 <= count1(%d) <= 10, 1 <= count(%d) <= 5 ",
                                first.count2, first.count), res);
                        //noinspection ConstantConditions
                        return res;
                    }
                });
    }

    @Test
    public void testByCustomHeaderAggregator() throws Exception {
        endpointByCustomHeader.reset();
        endpointByCustomHeader.expectedMessageCount(2);
        endpointByCustomHeaderInput.expectedHeaderReceived(BODY_CLASS,
                TestFailed.class.getName());
        endpointDependent.reset();
        endpointDependent.expectedMinimumMessageCount(4);

        sendTestEvent("by-custom-header", createTestStarted(), "customHeader", "1");
        sendTestEvent("by-custom-header", createTestFailed(), "customHeader", "2");
        sendTestEvent("by-custom-header", new StopByCustomHeader(), "customHeader", "1");
        sendTestEvent("by-custom-header", new StopByCustomHeader(), "customHeader", "2");
        endpointByCustomHeader.assertIsSatisfied(2000);
        endpointDependent.assertIsSatisfied(2000);
        expectExchangeExists(endpointByCustomHeader,
                "Output must contain test started",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null
                                && first.count == 0
                                && first.label.equals("1")
                                && first.label2.equals(first.label);
                    }
                });
        expectExchangeExists(endpointByCustomHeader,
                "Output must be test failed",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CounterState first = checkAndGetBytesInput(CounterState.class,
                                exchange.getIn().getBody(), classLoader);
                        return first != null
                                && first.count == 1
                                && first.label.equals("2")
                                && first.label2.equals(first.label);
                    }
                });

        Storage storage = processingEngine.getPlugin(ByCustomHeaderAggregator.class)
                                          .getContext()
                                          .getStorage();
        assertEquals(1, storage.get("count"));
        assertEquals("test", storage.get("string"));
        assertEquals(1.5, storage.get("double"));
        assertEquals(true, storage.get("boolean"));
        assertEquals("test", storage.get("propertyFromAppConfig"));
    }

    @Test
    public void testFallenRaisedAggregator() throws Exception {
        String uuid = uuid();
        final TestFailed testFailed = createTestFailed(uuid);
        endpointFallenRaisedOutput.reset();
        sendTestEvent("fallen-raised", testFailed, uuid);
        sendTestEvent("fallen-raised", copyOf(testFailed, TestDropped.class), uuid);
        endpointFallenRaisedOutput.expectedMinimumMessageCount(3);
        endpointFallenRaisedOutput.assertIsSatisfied(2000);
        Thread.sleep(5000);
        expectExchangeExists(endpointFallenRaisedOutput,
                "Output must contain testFailed ",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        TestFailed msg = checkAndGetBytesInput(TestFailed.class,
                                exchange.getIn().getBody(), classLoader);
                        logger.info("Checking if " + msg + " is instance of TestFailed...");
                        return msg != null && msg.getMethodname()
                                                 .equals(testFailed.getMethodname());
                    }
                });
        expectExchangeExists(endpointFallenRaisedOutput,
                "Output must contain string with method name",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        String msg = checkAndGetBytesInput(String.class,
                                exchange.getIn().getBody(), classLoader);
                        logger.info("Checking if " + msg + " is instance of String...");
                        return msg != null && msg.equals(testFailed.getMethodname());
                    }
                });
    }

    @Test
    public void testCustomFiltered() throws Exception {
        endpointCustomFilteredOutput.reset();
        endpointCustomFilteredOutput.expectedMessageCount(1);
        sendTestEvent("custom-filtered", createTestStarted("filtered"));
        sendTestEvent("custom-filtered", createTestFailed("filtered"));
        sendTestEvent("custom-filtered", createTestPassed("filtered"));
        sendTestEvent("custom-filtered", createTestBroken("filtered"));
        sendTestEvent("custom-filtered", new DerivedFilteredEvent());
        endpointCustomFilteredOutput.assertIsSatisfied(2000);
        expectExchangeExists(endpointCustomFilteredOutput,
                "Output must contain just filtered event",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CollectEventsState state = checkAndGetBytesInput(CollectEventsState.class,
                                exchange.getIn().getBody(), classLoader);
                        return state != null && state.collected.size() == 1;
                    }
                });
    }

    @Test
    public void testFiltered() throws Exception {
        endpointFilteredOutput.reset();
        endpointFilteredOutput.expectedMessageCount(1);
        sendTestEvent("filtered", createTestStarted("filtered"));
        sendTestEvent("filtered", createTestFailed("filtered"));
        sendTestEvent("filtered", createTestPassed("filtered"));
        sendTestEvent("filtered", createTestBroken("filtered"));
        sendTestEvent("filtered", new DerivedFilteredEvent());
        endpointFilteredOutput.assertIsSatisfied(2000);
        expectExchangeExists(endpointFilteredOutput,
                "Output must contain just filtered event",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        CollectEventsState state = checkAndGetBytesInput(CollectEventsState.class,
                                exchange.getIn().getBody(), classLoader);
                        return state != null && state.collected.size() == 1;
                    }
                });
    }

    @Test
    public void testByCustomStrategy() throws Exception {
        endpointByCustomStrategyOutput.reset();
        endpointByCustomStrategyOutput.expectedMessageCount(1);
        final TestFailed failed = createTestFailed("filtered");
        sendTestEvent("by-custom-strategy", failed);
        sendTestEvent("by-custom-strategy", createTestPassed("filtered"));
        sendTestEvent("by-custom-strategy", createTestBroken("filtered"));
        endpointByCustomStrategyOutput.assertIsSatisfied(2000);
        expectExchangeExists(endpointByCustomStrategyOutput,
                "Output must contain just one passed state",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        TestPassedState state = checkAndGetBytesInput(TestPassedState.class,
                                exchange.getIn().getBody(), classLoader);
                        return state != null
                                && state.getEvent()
                                        .getClassname()
                                        .equals(failed.getClassname());
                    }
                });
    }
}
