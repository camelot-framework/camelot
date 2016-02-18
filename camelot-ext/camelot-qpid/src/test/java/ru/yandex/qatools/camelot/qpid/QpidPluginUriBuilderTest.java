package ru.yandex.qatools.camelot.qpid;

import org.junit.Test;
import ru.yandex.qatools.camelot.config.Plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Ilya Sadykov
 */
public class QpidPluginUriBuilderTest {
    @Test
    public void testBuildValidUris() throws Exception {
        QpidPluginUriBuilder builder = new QpidPluginUriBuilder();

        assertThat(builder.basePluginUri(), is("amqp:queue:"));
        assertThat(builder.frontendBroadcastUri(), is("amqp:topic:frontend.notify;{create:always}"));
        final Plugin plugin = new Plugin();
        plugin.setBaseInputUri(builder.basePluginUri());
        plugin.setId("plugin.id");
        assertThat(builder.pluginUri(plugin, "", ""), is("amqp:queue:plugin.id;{create:always}"));
    }

}