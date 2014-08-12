package ru.yandex.qatools.camelot.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.yandex.qatools.camelot.config.Broker;
import ru.yandex.qatools.camelot.config.Parameter;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.config.PluginsSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 22.07.14
 */
@RunWith(Parameterized.class)
public class NameUtilTest {

    public String expected;

    public Plugin plugin;

    public NameUtilTest(String brokerConfig, List<Parameter> parameters, List<Parameter> defaults, String expected) {
        this.expected = expected;
        plugin = new Plugin();
        plugin.setBrokerConfig(brokerConfig);
        plugin.setBroker(new Broker());
        plugin.getBroker().getParams().addAll(parameters);

        plugin.setContext(new PluginContext());
        plugin.getContext().setSource(new PluginsSource());
        plugin.getContext().getSource().setBroker(new Broker());
        plugin.getContext().getSource().getBroker().getParams().addAll(defaults);

    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{"brokerConfig", params("A", "B"), params("C", "D"), "brokerConfig"},
                new Object[]{"brokerConfig", params(), params(), "brokerConfig"},
                new Object[]{null, params("A", "B"), params(), "?A=B"},
                new Object[]{null, params("A", "B"), params("A", "C"), "?A=B"},
                new Object[]{null, params("A", "B"), params("A", "C", "C", "D"), "?A=B&C=D"},
                new Object[]{null, params("A", "B"), params("C", "D"), "?A=B&C=D"},
                new Object[]{null, params("A", "B", "C", "D"), params("C", "F"), "?A=B&C=D"},
                new Object[]{null, params("A", "B"), params("B", "C"), "?A=B&B=C"},
                new Object[]{null, params(), params("B", "C"), "?B=C"},
                new Object[]{"", params("A", "B"), params(), "?A=B"},
                new Object[]{"", params("A", "B", "C", "D"), params(), "?A=B&C=D"}
        );
    }

    @Test
    public void pluginBrokerConfigTest() throws Exception {
        assertEquals(expected, NameUtil.pluginBrokerConfig(plugin));
    }

    public static List<Parameter> params(String... strings) {
        assertTrue("Count of strings must be even", strings.length % 2 == 0);

        List<Parameter> parameters = new ArrayList<>();
        Iterator<String> iter = Arrays.asList(strings).iterator();
        while (iter.hasNext()) {
            Parameter parameter = new Parameter();
            parameter.setName(iter.next());
            parameter.setValue(iter.next());
            parameters.add(parameter);
        }
        return parameters;
    }
}
