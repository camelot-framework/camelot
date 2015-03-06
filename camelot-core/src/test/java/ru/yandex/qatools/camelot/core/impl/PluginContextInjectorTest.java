package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.plugins.AggregatorWithContext;
import ru.yandex.qatools.camelot.core.plugins.AggregatorWithTimer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:camelot-core-context.xml", "classpath*:test-camelot-core-context.xml"})
@SuppressWarnings("unchecked")
@DirtiesContext(classMode = AFTER_CLASS)
public class PluginContextInjectorTest {

    @Autowired
    ProcessingEngine processingEngine;

    @Test
    public void testInjector() {
        PluginContextInjectorImpl injector = new PluginContextInjectorImpl();
        AggregatorWithContext agg = new AggregatorWithContext();
        Exchange exchange = mock(Exchange.class);
        Map<String, Object> headers = new HashMap<>();
        Message in = mock(Message.class);
        when(exchange.getIn()).thenReturn(in);
        when(in.getHeaders()).thenReturn(headers);
        injector.inject(agg, processingEngine.getPlugin(AggregatorWithTimer.class).getContext(), exchange);
        assertNotNull("Context must be injected", agg.repo1);
        assertNotNull("Context must be injected", agg.repo2);
        assertNotNull("Context must be injected", agg.repo3);
        assertNotNull("Context must be injected", agg.clientSender);
        assertNotNull("Context must be injected", agg.config);
        assertNotNull("Context must be injected", agg.configValue);
        assertNotNull("Context must be injected", agg.headers);
        assertNotNull("Context must be injected", agg.input1);
        assertNotNull("Context must be injected", agg.input2);
        assertNotNull("Context must be injected", agg.input3);
        assertNotNull("Context must be injected", agg.listener);
        assertNotNull("Context must be injected", agg.output1);
        assertNotNull("Context must be injected", agg.output2);
        assertNotNull("Context must be injected", agg.output3);
        assertNotNull("Context must be injected", agg.plugin1);
        assertNotNull("Context must be injected", agg.plugin2);
        assertNotNull("Context must be injected", agg.plugins);
        assertNotNull("Context must be injected", agg.storage);
        assertNotNull("Context must be injected", agg.storage1);
        assertNotNull("Context must be injected", agg.storage2);
        assertNotNull("Context must be injected", agg.injectableComponent);
        assertEquals("Context must be injected", agg.input1, agg.injectableComponent.input);
        assertEquals("Plugin storage must be injected", agg.storage1, agg.injectableComponent.otherPluginStorage);
        assertNotNull("Context must be injected", agg.injectableInterface);
        assertEquals("Context must be injected", agg.input1, ((InjectableInterfaceImpl) agg.injectableInterface).input);
        assertEquals("Plugin storage must be injected", agg.storage1, ((InjectableInterfaceImpl) agg.injectableInterface).otherPluginStorage);
    }
}
