package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.apache.camel.test.spring.MockEndpoints;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.BasicAggregatorsTest;
import ru.yandex.qatools.camelot.core.beans.TestSkipped;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedAggregator;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:camelot-core-context.xml",
        "classpath*:test-camelot-core-context.xml",
        "classpath*:test-camelot-unreachable-repo.xml"
})
@DirtiesContext(classMode = AFTER_CLASS)
@MockEndpoints("*")
@SuppressWarnings("unchecked")
public class DelayedRouteTest extends BasicAggregatorsTest {


    @Test
    public void testAllSkippedWithUnreachableRepo() throws Exception {
        endpointAllSkippedInput.reset();
        endpointAllSkippedInput.expectedMinimumMessageCount(2);

        sendEvent(AllSkippedAggregator.class, new TestSkipped());

        sleep(2000);
        endpointAllSkippedInput.assertIsSatisfied(500);
        assertThat("We must not retry send more often than once a second",
                endpointAllSkippedInput.getExchanges().size(), lessThan(4));
    }
}
