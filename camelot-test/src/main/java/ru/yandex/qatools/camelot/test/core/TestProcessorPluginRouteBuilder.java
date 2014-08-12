package ru.yandex.qatools.camelot.test.core;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.builders.ProcessorRoutesBuilder;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
class TestProcessorPluginRouteBuilder implements ProcessorRoutesBuilder {
    final ProcessorRoutesBuilder original;

    public TestProcessorPluginRouteBuilder(ClassLoader classLoader, Class procClass, Object procMock, ProcessorRoutesBuilder original, Plugin plugin) throws Exception {
        this.original = original;
        original.setProcessor(
                new TestCamelotProcessor(classLoader, procClass, procMock, original.getProcessor(), plugin)
        );
    }

    @Override
    public void startRoutes() throws Exception {
        original.startRoutes();
    }

    @Override
    public void removeRoutes() throws Exception {
        original.removeRoutes();
    }

    @Override
    public void setProcessor(Processor processor) {
        original.setProcessor(processor);
    }

    @Override
    public Processor getProcessor() {
        return original.getProcessor();
    }

    @Override
    public void addRoutesToCamelContext(CamelContext context) throws Exception {
        original.addRoutesToCamelContext(context);
    }
}
