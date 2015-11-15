package ru.yandex.qatools.camelot.common;

import org.junit.Test;
import ru.yandex.qatools.camelot.api.AppConfig;
import ru.yandex.qatools.camelot.api.annotations.Config;
import ru.yandex.qatools.camelot.api.annotations.ConfigValue;
import ru.yandex.qatools.camelot.api.annotations.OnClientMessage;
import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;

import java.io.Serializable;

import static org.mockito.Mockito.*;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class PluginAnnotatedMethodInvokerTest {
    private static TestAggregator aggMock = mock(TestAggregator.class);

    public static class TestState implements Serializable {
    }

    public interface TestAggregator {
        @OnInit
        void onInit1();

        @OnInit
        void onInit2();
    }

    public static class TestAggregatorImpl implements TestAggregator {
        @Config
        AppConfig config;

        @ConfigValue("test.key")
        String testKey;

        @Override
        @OnClientMessage
        public void onInit1() {
            aggMock.onInit1();
        }

        @Override
        @OnClientMessage
        public void onInit2() {
            aggMock.onInit2();
            config.getProperty("other.key");
        }
    }

    /**
     * Bad style test :(
     */
    @Test
    public void test() throws Exception {
        Plugin plugin = new Plugin();
        PluginContext pluginContext = new PluginContext();
        pluginContext.setClassLoader(getClass().getClassLoader());
        pluginContext.setPluginClass(TestAggregatorImpl.class.getName());
        final AppConfig appConfig = mock(AppConfig.class);
        pluginContext.setAppConfig(appConfig);
        pluginContext.setInjector(new PluginContextInjectorImpl());
        plugin.setAggregator(TestAggregatorImpl.class.getName());
        plugin.setContext(pluginContext);
        pluginContext.setPluginsService(mock(PluginsService.class));
        when(pluginContext.getPluginsService().getAppConfig()).thenReturn(appConfig);

        PluginMethodInvoker invoker = new PluginAnnotatedMethodInvoker(plugin, OnClientMessage.class).process();
        invoker.invoke();

        verify(aggMock).onInit1();
        verify(appConfig, times(2)).getProperty("test.key");
        verify(appConfig).getProperty("other.key");
        verify(aggMock).onInit2();
        verifyNoMoreInteractions(aggMock);
    }
}
