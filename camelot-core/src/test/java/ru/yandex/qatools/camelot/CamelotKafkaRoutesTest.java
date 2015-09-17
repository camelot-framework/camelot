package ru.yandex.qatools.camelot;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.core.beans.StopEvent;
import ru.yandex.qatools.camelot.core.beans.UndefinedState;

import static java.util.UUID.randomUUID;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:test-camelot-kafka-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
@MockEndpoints("*")
public class CamelotKafkaRoutesTest extends BasicPluginsTest {

    private StopEvent event;

    @Before
    public void setUp() throws Exception {
        endpointStop.reset();
        event = new StopEvent();
        event.setMethodname(randomUUID().toString());
    }

    @Test
    public void testKafkaSimpleRouteIsWorking() throws Exception {
        endpointStop.expectedMessageCount(1);
        processingEngine.getPlugin("bind-to-output").getContext().getInput().produce(event);
        endpointStop.assertIsSatisfied(5000);
        expectExchangeExists(endpointStop,
                "Output must contain just filtered event",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        UndefinedState state = getInput(exchange, UndefinedState.class);
                        return state != null && state.getEvent().getMethodname().equals(event.getMethodname());
                    }
                });
    }

    @Test
    public void testKafkaComplexRouteIsWorking() throws Exception {
        endpointStop.expectedMessageCount(2);
        processingEngine.getPlugin("send-to-output").getContext().getInput().produce(event);
        endpointStop.assertIsSatisfied(5000);
        expectExchangeExists(endpointStop,
                "Output must contain just filtered event",
                new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        UndefinedState state = getInput(exchange, UndefinedState.class);
                        return state != null && state.getEvent().getMethodname().equals(event.getMethodname());
                    }
                });
    }

}
