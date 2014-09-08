package ru.yandex.qatools.camelot.test.core;

import net.sf.cglib.proxy.Enhancer;
import ru.yandex.qatools.camelot.config.PluginContext;
import ru.yandex.qatools.camelot.test.AggregatorStateStorage;

import java.util.Set;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
class AggregatorStateStorageImpl implements AggregatorStateStorage {

    private static final String TO_STRING_INDENT = "        ";

    private final PluginContext context;
    private final StateLoader stateLoader;

    AggregatorStateStorageImpl(PluginContext context) {
        this.context = context;
        stateLoader = new StateLoader(context);
    }

    @Override
    public <T> T getActual(String key) {
        return stateLoader.fetchState(key);
    }

    @Override
    public <T> T get(Class<T> stateClass, String key) {
        Enhancer e = new Enhancer();
        e.setSuperclass(stateClass);
        e.setCallback(new StateMethodInvocationHandler(stateLoader, stateClass, key));
        //noinspection unchecked
        return (T) e.create();
    }

    @Override
    public Set<String> keys() {
        return context.getAggregationRepo().getKeys();
    }

    @Override
    public String toString() {
        if (keys().isEmpty()) {
            return "an empty AggregatorStateStorage";
        }

        StringBuilder builder = new StringBuilder()
                .append("AggregatorStateStorage containing [\n");

        for (String key : keys()) {
            builder.append(TO_STRING_INDENT)
                    .append(key)
                    .append(" -> ")
                    .append(reflectionToString(getActual(key), SHORT_PREFIX_STYLE))
                    .append("\n");
        }

        builder.append("]");
        return builder.toString();
    }
}
