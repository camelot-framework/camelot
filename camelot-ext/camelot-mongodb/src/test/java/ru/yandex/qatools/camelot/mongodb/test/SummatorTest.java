package ru.yandex.qatools.camelot.mongodb.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import ru.yandex.qatools.camelot.test.*;
import ru.yandex.qatools.embed.service.MongoEmbeddedService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.qatools.camelot.api.Constants.Keys.ALL;

/**
 * @author Ilya Sadykov
 */
@RunWith(CamelotTestRunner.class)
public class SummatorTest {
    protected MongoEmbeddedService mongo;
    @Helper
    TestHelper helper;
    @PluginMock
    Summator summator;
    @AggregatorState(Summator.class)
    AggregatorStateStorage summatorRepo;

    @Test
    public void testSummator() throws Exception {
        helper.sendTo(Summator.class, 1);
        helper.sendTo(Summator.class, 2);
        verify(summator, timeout(2000L).times(2)).onSum(any(AtomicInteger.class), anyInt());
        reset(summator);
        assertThat(((AtomicInteger) summatorRepo.getActual(ALL)).get(), equalTo(3));
        helper.sendTo(Summator.class, -5);
        verify(summator, timeout(2000L)).onSum(any(AtomicInteger.class), anyInt());
        assertThat(((AtomicInteger) summatorRepo.getActual(ALL)).get(), equalTo(-2));
    }
}