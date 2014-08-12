package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.AggregationRepository;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import ru.yandex.qatools.camelot.api.*;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.core.AnnotatedFieldListener;
import ru.yandex.qatools.camelot.core.ProcessingEngine;
import ru.yandex.qatools.camelot.core.builders.QuartzInitializer;
import ru.yandex.qatools.camelot.test.*;
import ru.yandex.qatools.camelot.test.service.MockedClientSenderInitializer;
import ru.yandex.qatools.camelot.test.service.TestHelperImpl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.camel.util.CamelContextHelper.getEndpointInjection;
import static org.apache.camel.util.ObjectHelper.isEmpty;
import static org.mockito.Mockito.reset;
import static org.slf4j.LoggerFactory.getLogger;
import static ru.yandex.qatools.camelot.test.CamelotTestRunner.REAL_TEST_CLASS_ATTR;
import static ru.yandex.qatools.camelot.test.core.TestUtil.preparePluginMock;
import static ru.yandex.qatools.camelot.util.ContextUtils.autowireFields;
import static ru.yandex.qatools.camelot.util.ExceptionUtil.formatStackTrace;
import static ru.yandex.qatools.camelot.util.IOUtils.readResource;
import static ru.yandex.qatools.camelot.util.ReflectUtil.*;
import static ru.yandex.qatools.camelot.util.ServiceUtil.injectAnnotatedField;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class CamelotTestListener extends AbstractTestExecutionListener {
    final Logger logger = getLogger(getClass());

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        injectTestContext(testContext);
        final ProcessingEngine engine = getAppContext(testContext).getBean(ProcessingEngine.class);
        for (Plugin plugin : engine.getPluginsMap().values()) {
            final PluginEndpoints endpoints = plugin.getContext().getEndpoints();
            getPluginMockEndpoint(engine.getCamelContext(), endpoints.getOutputUri()).reset();
            getPluginMockEndpoint(engine.getCamelContext(), endpoints.getInputUri()).reset();
        }
    }

    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        clearContext(testContext);
    }

    private void clearContext(TestContext testContext) throws Exception {
        final ApplicationContext applicationContext = getAppContext(testContext);
        final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
        final MockedClientSenderInitializer clientInitializer = applicationContext.getBean(MockedClientSenderInitializer.class);
        final TestBuildersFactory factory = ((TestBuildersFactory) engine.getBuildersFactory());
        final CamelContext camelContext = engine.getCamelContext();
        for (Plugin plugin : engine.getPluginsMap().values()) {
            final Object mock = factory.getMocksStorage().get(plugin.getId());
            reset(mock);
            preparePluginMock(plugin, mock);
        }
        for (MockedClientSenderInitializer.Provider provider : clientInitializer.getClientSenders().values()) {
            for (Object mock : provider.getClientSenders().values()) {
                reset(mock);
            }
        }
        for (Plugin plugin : engine.getPluginsMap().values()) {
            final PluginContext context = plugin.getContext();
            final AggregationRepository repo = context.getAggregationRepo();
            for (String key : repo.getKeys()) {
                final Exchange exchange = repo.get(camelContext, key);
                repo.remove(camelContext, key, exchange);
            }

            // we need to reinitialize the plugin as we have cleared it's state
            engine.getPluginInitializer().init(plugin);
        }
    }

    private ApplicationContext getAppContext(TestContext testContext) {
        try {
            return testContext.getApplicationContext();
        } catch (Exception e) {
            logger.error("Failed to load Spring context: \n" + formatStackTrace(e) , e);
            throw new RuntimeException("Failed to load Spring context", e);
        }
    }

    @Override
    public void afterTestClass(TestContext testContext) throws Exception {
        final ApplicationContext applicationContext = getAppContext(testContext);
        final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
        // reset context injector
        ((TestContextInjector) engine.getContextInjector()).reset();

        // return the original app config to each plugin
        for (Plugin plugin : engine.getPluginsMap().values()) {
            final PluginContext context = plugin.getContext();
            if (context.getAppConfig() instanceof WrappedAppConfig) {
                context.setAppConfig(((WrappedAppConfig) context.getAppConfig()).getOriginal());
            }
        }
    }

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        final ApplicationContext applicationContext = getAppContext(testContext);
        final Class testClass = getTestClass(testContext);

        // Load additional properties from the files
        if (applicationContext != null) {
            // override the app config for each plugin
            if (getAnnotation(testClass, UseProperties.class) != null) {
                try {
                    final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
                    String[] locations = (String[]) getAnnotationValue(testClass, UseProperties.class, "value");
                    final Properties properties = new Properties();
                    for (String location : locations) {
                        for (org.springframework.core.io.Resource path : resolveResourcesFromPattern(location, testClass)) {
                            properties.load(readResource(path.getURL()));
                        }
                    }
                    for (Plugin plugin : engine.getPluginsMap().values()) {
                        final AppConfig originConfig = plugin.getContext().getAppConfig();
                        plugin.getContext().setAppConfig(new WrappedAppConfig(originConfig, properties));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not use @UseProperties along with your test.", e);
                }
            }

            // stop quartz if
            if (getAnnotation(testClass, DisableTimers.class) != null) {
                try {
                    final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
                    final TestBuildersFactory factory = (TestBuildersFactory) engine.getBuildersFactory();

                    for (QuartzInitializer initializer : factory.getQuartzInitializers()) {
                        initializer.standby();
                    }

                } catch (Exception e) {
                    throw new RuntimeException("Could not use @DisableTimers along with your test.", e);
                }
            }

            // Read test config
            if (getAnnotation(testClass, CamelotTestConfig.class) != null) {
                try {
                    final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
                    final TestContextInjector injector = (TestContextInjector) engine.getContextInjector();

                    // override necessary components
                    OverrideComponent[] components = (OverrideComponent[])
                            getAnnotationValue(testClass, CamelotTestConfig.class, "components");
                    for (OverrideComponent component : components) {
                        injector.overrideComponent(component.from(), component.to());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not use @CamelotTestConfig.", e);
                }
            }
        }
    }

    private Class getTestClass(TestContext testContext) {
        final Class res = (Class) testContext.getAttribute(REAL_TEST_CLASS_ATTR);
        return (res != null) ? res : testContext.getTestClass();
    }

    private void injectTestContext(TestContext testContext) {
        final Class testClass = getTestClass(testContext);
        final Object testObject = testContext.getTestInstance();
        final ApplicationContext applicationContext = getAppContext(testContext);
        try {
            injectTestContextToInstance(testClass, testObject, applicationContext);
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare camelot test context", e);
        }
    }

    private void injectTestContextToInstance(final Class clazz, final Object instance, final ApplicationContext applicationContext) throws Exception {
        final ProcessingEngine engine = applicationContext.getBean(ProcessingEngine.class);
        final CamelContext camelContext = engine.getCamelContext();
        if (!(engine.getBuildersFactory() instanceof TestBuildersFactory)) {
            throw new RuntimeException("Failed to inject test builders factory into the engine!");
        }
        final TestBuildersFactory factory = (TestBuildersFactory) engine.getBuildersFactory();
        final MockedClientSenderInitializer clientSenders = applicationContext.getBean(MockedClientSenderInitializer.class);

        injectAnnotatedField(clazz, instance, TestComponent.class, new AnnotatedFieldListener<Object, TestComponent>() {
            @Override
            public Object found(Field field, TestComponent annotation) throws Exception {
                Class fieldType = field.getType();
                Object value = fieldType.newInstance();
                injectTestContextToInstance(fieldType, value, applicationContext);
                return value;
            }
        });

        injectAnnotatedField(clazz, instance, ClientSenderMock.class, new AnnotatedFieldListener<ClientMessageSender, ClientSenderMock>() {
            @Override
            public ClientMessageSender found(Field field, ClientSenderMock annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, ClientSenderMock.class);
                try {
                    final String topic = (String) getAnnotationValue(annotation, "topic");
                    final ClientSendersProvider provider = clientSenders.getClientSenders().get(pluginId);
                    if (provider == null) {
                        throw new RuntimeException("Senders provider not found!");
                    }
                    return provider.getSender(topic);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject client sender for plugin " + pluginId, e);
                }
            }
        });

        injectAnnotatedField(clazz, instance, EndpointPluginInput.class, new AnnotatedFieldListener<MockEndpoint, EndpointPluginInput>() {
            @Override
            public MockEndpoint found(Field field, EndpointPluginInput annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, EndpointPluginInput.class);
                final PluginContext plugin = engine.getPluginContext(pluginId);
                try {
                    return getPluginMockEndpoint(camelContext, plugin.getEndpoints().getInputUri());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject plugin mock for plugin " + pluginId, e);
                }
            }
        });

        injectAnnotatedField(clazz, instance, EndpointPluginOutput.class, new AnnotatedFieldListener<MockEndpoint, EndpointPluginOutput>() {
            @Override
            public MockEndpoint found(Field field, EndpointPluginOutput annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, EndpointPluginOutput.class);
                final PluginContext plugin = engine.getPluginContext(pluginId);
                try {
                    return getPluginMockEndpoint(camelContext, plugin.getEndpoints().getOutputUri());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject plugin mock for plugin " + pluginId, e);
                }
            }
        });

        injectAnnotatedField(clazz, instance, PluginMock.class, new AnnotatedFieldListener<Object, PluginMock>() {
            @Override
            public Object found(Field field, PluginMock annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, PluginMock.class);
                try {
                    return factory.getMocksStorage().get(pluginId);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject plugin mock for plugin " + pluginId, e);
                }
            }
        });
        injectAnnotatedField(clazz, instance, AggregatorState.class, new AnnotatedFieldListener<AggregatorStateStorageImpl, AggregatorState>() {
            @Override
            public AggregatorStateStorageImpl found(Field field, AggregatorState annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, AggregatorState.class);
                try {
                    final PluginContext pluginContext = engine.getPluginContext(pluginId);
                    if (pluginContext == null) {
                        throw new RuntimeException("Plugin context not found!");
                    }
                    return new AggregatorStateStorageImpl(pluginContext);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject aggregator state for plugin " + pluginId, e);
                }

            }
        });
        injectAnnotatedField(clazz, instance, Helper.class, new AnnotatedFieldListener<TestHelperImpl, Helper>() {
            @Override
            public TestHelperImpl found(Field field, Helper annotation) throws Exception {
                return applicationContext.getBean(TestHelperImpl.class);
            }
        });
        injectAnnotatedField(clazz, instance, Resource.class, new AnnotatedFieldListener<Object, Resource>() {
            @Override
            public Object found(Field field, Resource annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, Resource.class);
                try {
                    final Plugin plugin = engine.getPlugin(pluginId);
                    if (plugin.getResource() != null) {
                        Object res = Class.forName(plugin.getResource()).newInstance();
                        plugin.getContext().getInjector().inject(res, plugin.getContext(), null);
                        return res;
                    }
                    return null;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject resource for plugin " + pluginId, e);
                }
            }
        });
        injectAnnotatedField(clazz, instance, PluginOutputListener.class, new AnnotatedFieldListener<EndpointListener, PluginOutputListener>() {
            @Override
            public EndpointListener found(Field field, PluginOutputListener annotation) throws Exception {
                final String pluginId = calcPluginId(field, engine, PluginOutputListener.class);
                final PluginContext pluginContext = engine.getPluginContext(pluginId);
                if (pluginContext == null) {
                    throw new RuntimeException("Plugin context not found: " + pluginId);
                }
                try {
                    return pluginContext.getListener();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to inject listener state for plugin " + pluginId, e);
                }
            }
        });
        autowireFields(instance, applicationContext, camelContext);
    }

    private MockEndpoint getPluginMockEndpoint(CamelContext camelContext, String uri) {
        return (MockEndpoint) getEndpointInjection(camelContext, mockedUri(uri), "", "", true);
    }

    private String mockedUri(String uri) {
        return format("mock:%s", uri).replaceAll("^([^?]+)(\\?.+)?$", "$1");
    }

    private String calcPluginId(Field field, ProcessingEngine engine, Class<? extends Annotation> aClass) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            String pluginId = (String) getAnnotationValue(field, aClass, "id");
            if (isEmpty(pluginId)) {
                Class pluginClass = (Class) getAnnotationValue(field, aClass, "value");
                if (!pluginClass.equals(aClass)) {
                    return engine.getPlugin(pluginClass).getId();
                }
            }
            return pluginId;
        } catch (NoSuchMethodException | IllegalAccessException e) {
            logger.trace("Ignored exception", e);
            return engine.getPlugin(field.getType()).getId();
        }
    }

    private void setFieldValue(Object res, Field field, Object value) throws IllegalAccessException {
        if (value == null) {
            throw new RuntimeException("Failed to inject the field " + field.getName() + " to the object: " + res);
        }
        boolean oldAccessible = field.isAccessible();
        field.setAccessible(true);
        field.set(res, value);
        field.setAccessible(oldAccessible);
    }
}
