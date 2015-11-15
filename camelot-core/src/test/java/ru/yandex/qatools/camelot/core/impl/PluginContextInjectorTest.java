package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import ru.yandex.qatools.camelot.common.PluginContextInjectorImpl;
import ru.yandex.qatools.camelot.common.ProcessingEngine;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.plugins.AggregatorWithContext;
import ru.yandex.qatools.camelot.core.plugins.AggregatorWithTimer;
import ru.yandex.qatools.camelot.core.plugins.AllSkippedAggregator;
import ru.yandex.qatools.camelot.core.service.TestBeanWithContext;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:camelot-core-context.xml", "classpath*:test-camelot-core-context.xml"})
@SuppressWarnings("unchecked")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PluginContextInjectorTest {

    @Autowired
    ProcessingEngine processingEngine;

    @Autowired
    TestBeanWithContext beanWithContext;

    @Test
    public void testPluginsContextInjectedIntoBean() {
        assertBasicContextInjected(beanWithContext);
    }

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
        assertBasicContextInjected(agg);
        assertNotNull("Context must be injected", agg.repo);
        assertNotNull("Context must be injected", agg.clientSender);
        assertNotNull("Context must be injected", agg.config);
        assertNotNull("Context must be injected", agg.headers);
        assertNotNull("Context must be injected", agg.input);
        assertNotNull("Context must be injected", agg.output);
        assertNotNull("Context must be injected", agg.storage);
        assertNotNull("Context must be injected", agg.injectableComponent);
        assertEquals("Context must be injected", agg.input, agg.injectableComponent.input);
        assertEquals("Plugin storage must be injected", agg.storage1, agg.injectableComponent.otherPluginStorage);
        assertNotNull("Context must be injected", agg.injectableInterface);
        Assert.assertEquals("Context must be injected", agg.input, ((InjectableInterfaceImpl) agg.injectableInterface).input);
        Assert.assertEquals("Plugin storage must be injected", agg.storage1, ((InjectableInterfaceImpl) agg.injectableInterface).otherPluginStorage);
    }

    private void assertBasicContextInjected(TestBeanWithContext object) {
        PluginContext allSkippedContext = processingEngine.getPluginContext(AllSkippedAggregator.class);
        assertNotNull("Context must be injected", object.repo2);
        assertNotNull("Context must be injected", object.repo3);
        assertNotNull("Context must be injected", object.config);
        assertNotNull("Context must be injected", object.configValue);
        assertNotNull("Context must be injected", object.input2);
        assertNotNull("Context must be injected", object.input3);
        assertNotNull("Context must be injected", object.output2);
        assertNotNull("Context must be injected", object.output3);
        assertNotNull("Context must be injected", object.plugin1);
        assertNotNull("Context must be injected", object.plugin2);
        assertNotNull("Context must be injected", object.plugins);
        assertNotNull("Context must be injected", object.storage1);
        assertNotNull("Context must be injected", object.storage2);
        assertEquals("Plugin storage must be injected", object.storage1, allSkippedContext.getStorage());
        assertEquals("Plugin repo must be injected", object.repo2, allSkippedContext.getRepository());
        assertEquals("Plugin repo must be injected", object.repo3, allSkippedContext.getRepository());
        assertEquals("Plugin input must be injected", object.input2, allSkippedContext.getInput());
        assertEquals("Plugin input must be injected", object.input3, allSkippedContext.getInput());
        assertEquals("Plugin output must be injected", object.output2, allSkippedContext.getOutput());
        assertEquals("Plugin output must be injected", object.output3, allSkippedContext.getOutput());
    }
}
