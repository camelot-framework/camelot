package ru.yandex.qatools.camelot.mongodb;

import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import ru.yandex.qatools.camelot.api.ClientMessageSender;
import ru.yandex.qatools.camelot.common.PluginsService;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.mongodb.test.SaveAggregator;
import ru.yandex.qatools.camelot.test.CamelotTestRunner;

import static java.util.stream.IntStream.rangeClosed;
import static ru.yandex.qatools.camelot.api.Constants.Headers.BODY_CLASS;
import static ru.yandex.qatools.camelot.api.Constants.Headers.TOPIC;

/**
 * @author Ilya Sadykov
 */
@RunWith(CamelotTestRunner.class)
public class MongodbClientSendersProviderTest {

    @Autowired
    PluginsService pluginsService;

    @Value("${camelot.mongodb.dbname}")
    String dbName;

    @Autowired
    MongodbClientSendersProvider provider;

    @EndpointInject(uri = "mock:direct:frontend.notify")
    protected MockEndpoint feNotify;

    @Test
    public void testMessagesAreSentFromQueueFrontend() throws Exception {
        final Plugin plugin = pluginsService.getPlugin(SaveAggregator.class);
        final PluginContext ctx = plugin.getContext();
        final ClientMessageSender sender = provider.getSender("topic", plugin.getId(), ctx.getEndpoints().getFrontendSendUri());

        feNotify.expectedMessageCount(10);
        feNotify.expectedHeaderReceived(TOPIC, "topic");
        feNotify.expectedHeaderReceived(BODY_CLASS, String.class.getName());
        rangeClosed(1, 10).forEach(n -> sender.send("Hello" + n));
        feNotify.assertIsSatisfied(3000L);
    }
}