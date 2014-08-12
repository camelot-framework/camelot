package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.processor.aggregate.MemoryAggregationRepository;
import org.junit.Before;
import org.junit.Test;
import org.quartz.Scheduler;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.api.annotations.OnTimer;
import ru.yandex.qatools.camelot.api.annotations.Repository;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.impl.PluginContextInjectorImpl;

import static org.mockito.Mockito.*;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class QuartzAggregatorSchedulerBuilderTest {

    final static TestClass mock = mock(TestClass.class);
    private CamelContext camelContext;
    private Scheduler scheduler;
    private Plugin plugin;

    public static class TestClass {
        @Repository
        AggregatorRepository repo;

        @OnTimer
        public void method1(String state) {
            mock.method1(state);
        }

        @OnTimer
        public void method2(String state) {
            mock.method2(state);
        }

        @OnTimer(perState = false)
        public void method3() {
            if (repo == null) {
                throw new RuntimeException("repository must not be null!");
            }
            mock.method3();
        }
    }

    @Before
    public void init() {
        camelContext = mock(CamelContext.class);
        Exchange exchange1 = exchange("test1");
        Exchange exchange2 = exchange("test2");
        scheduler = mock(Scheduler.class);
        plugin = new Plugin();
        plugin.setId("test-plugin");
        plugin.setAggregator(TestClass.class.getName());
        plugin.setContext(new PluginContext());
        plugin.getContext().setClassLoader(getClass().getClassLoader());
        plugin.getContext().setPluginClass(TestClass.class.getName());
        plugin.getContext().setInjector(new PluginContextInjectorImpl());
        plugin.getContext().setRepository(mock(AggregatorRepository.class));

        final MemoryAggregationRepository repo = new MemoryAggregationRepository();
        plugin.getContext().setAggregationRepo(repo);
        repo.add(camelContext, "key1", exchange1);
        repo.add(camelContext, "key2", exchange2);
    }

    private Exchange exchange(String body) {
        Exchange result = mock(Exchange.class);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn(body);
        when(result.getIn()).thenReturn(message);
        when(message.getHeader(BODY_CLASS)).thenReturn(String.class.getName());
        return result;
    }


    @Test
    public void testInvokeJobs() throws Exception {
        new QuartzAggregatorSchedulerBuilder(camelContext, scheduler, plugin).invokeJobs();
        verify(mock).method1("test1");
        verify(mock).method2("test1");
        verify(mock).method1("test2");
        verify(mock).method2("test2");
        verify(mock).method3();
    }

}
