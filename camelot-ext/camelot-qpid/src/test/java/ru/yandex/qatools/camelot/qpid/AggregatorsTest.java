package ru.yandex.qatools.camelot.qpid;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.qpid.plugins.SumAggregator;
import ru.yandex.qatools.camelot.test.*;

import javax.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
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
}
