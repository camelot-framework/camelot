package ru.yandex.qatools.camelot.mongodb.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.test.AggregatorState;
import ru.yandex.qatools.camelot.test.AggregatorStateStorage;
import ru.yandex.qatools.camelot.test.CamelotTestRunner;
import ru.yandex.qatools.camelot.test.Helper;
import ru.yandex.qatools.camelot.test.PluginMock;
import ru.yandex.qatools.camelot.test.TestHelper;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.api.Constants.Keys.ALL;

/**
 * @author Ilya Sadykov
 * @author Innokenty Shuvalov
 */
@RunWith(CamelotTestRunner.class)
public class AggregatorsTest {

    @Helper
    TestHelper helper;

    @PluginMock
    SumAggregator sumAggregator;

    @PluginMock
    SaveAggregator saveAggregator;

    @AggregatorState(SumAggregator.class)
    AggregatorStateStorage sumAggregatorRepo;

    @Inject
    PluginsService pluginsService;

    @Test
    public void testAggregation() throws Exception {
        helper.sendTo(SumAggregator.class, 1);
        helper.sendTo(SumAggregator.class, 2);

        verify(sumAggregator, timeout(2000L).times(2)).onInteger(any(AtomicInteger.class), anyInt());
        reset(sumAggregator);
        Thread.sleep(500);
        assertThat(((AtomicInteger) sumAggregatorRepo.getActual(ALL)).get(), equalTo(3));

        helper.sendTo(SumAggregator.class, -5);
        verify(sumAggregator, timeout(2000L)).onInteger(any(AtomicInteger.class), anyInt());
        Thread.sleep(500);
        assertThat(((AtomicInteger) sumAggregatorRepo.getActual(ALL)).get(), equalTo(-2));
    }

    @Test
    public void testStorage() throws Exception {
        helper.sendTo(SaveAggregator.class, 1);
        helper.sendTo(SaveAggregator.class, 2);

        verify(saveAggregator, timeout(2000L).times(2)).onInteger(any(AtomicInteger.class), anyInt());
        Thread.sleep(500);

        //noinspection unchecked
        Map<String, AtomicInteger> states
                = pluginsService.getPluginContext(SaveAggregator.class).getRepository().valuesMap();
        assertThat(states.size(), is(2));
        assertThat(states.get("1").get(), is(1));
        assertThat(states.get("2").get(), is(2));
    }
}
