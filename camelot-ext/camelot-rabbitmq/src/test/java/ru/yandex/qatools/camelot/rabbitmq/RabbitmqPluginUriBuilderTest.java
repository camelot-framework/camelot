package ru.yandex.qatools.camelot.rabbitmq;

import org.junit.Test;
import ru.yandex.qatools.camelot.config.Plugin;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Ilya Sadykov
 */
public class RabbitmqPluginUriBuilderTest {
    @Test
    public void testBuildValidUris() throws Exception {
        RabbitmqPluginUriBuilder builder = new RabbitmqPluginUriBuilder("localhost:1,localhost:2", "");

        assertThat(builder.basePluginUri(), is("rabbitmq://localhost:1/"));
        assertThat(builder.frontendBroadcastUri(), is("rabbitmq://localhost:1/frontend.notify" +
                "?queue=frontend.notify&addresses=localhost:1,localhost:2&exchangeType=topic"));
        final Plugin plugin = new Plugin();
        plugin.setBaseInputUri(builder.basePluginUri());
        plugin.setId("plugin.id");
        assertThat(builder.pluginUri(plugin, "", ""), is("rabbitmq://localhost:1/plugin.id" +
                "?queue=plugin.id&addresses=localhost:1,localhost:2"));
    }
}