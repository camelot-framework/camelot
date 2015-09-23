package ru.yandex.qatools.camelot;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.common.MockedBroadcastService;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static ru.yandex.qatools.camelot.core.util.TestEventGenerator.createTestBroken;
import static ru.yandex.qatools.camelot.core.util.TestEventGenerator.createTestStarted;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:camelot-web-context.xml", "classpath:test-web.spring-context.xml"})
@DirtiesContext(classMode = AFTER_CLASS)
@MockEndpoints("*")
public class BroadcastAggregatorsTest extends ActivemqAggregatorsTest {

    @Autowired
    protected MockedBroadcastService mockedBroadcastService;

    @Test
    public void testByLabelBrokenAggregator() throws Exception {
        final String uuid = uuid();
        for (int i = 0; i < 2; ++i) {
            sendTestEvent("lifecycle", createTestStarted(uuid, "label1", "label2"), uuid + i);
            sendTestEvent("lifecycle", createTestBroken(uuid, "label1", "label2"), uuid + i);
        }
        verify(mockedBroadcastService.getBroadcaster("broken-by-label", "topic1"), timeout(5000).times(4)).send(startsWith("label"));
        verify(mockedBroadcastService.getBroadcaster("broken-by-label", "topic2"), timeout(5000).times(4)).send(eq("hello2"));
    }
}
