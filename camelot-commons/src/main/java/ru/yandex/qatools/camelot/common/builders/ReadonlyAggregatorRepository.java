package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.AggregatorRepository;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithLocalKeys;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithLocks;
import ru.yandex.qatools.camelot.common.AggregationRepositoryWithValuesMap;
import ru.yandex.qatools.camelot.config.Plugin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 * @author Innokenty Shuvalov (mailto: innokenty@yandex-team.ru)
 */
public class ReadonlyAggregatorRepository<T extends Serializable> implements AggregatorRepository<T> {

    private final Plugin plugin;
    private final CamelContext camelContext;

    public ReadonlyAggregatorRepository(CamelContext camelContext, Plugin plugin) {
        this.plugin = plugin;
        this.camelContext = camelContext;
    }

    @Override
    public T get(String key) {
        AggregationRepository repo = plugin.getContext().getAggregationRepo();

        Exchange exchange = (repo instanceof AggregationRepositoryWithLocks)
                ? ((AggregationRepositoryWithLocks) repo).getWithoutLock(camelContext, key)
                : repo.get(camelContext, key);

        return deserialize(exchange);
    }

    @Override
    public Set<String> keys() {
        return plugin.getContext().getAggregationRepo().getKeys();
    }

    @Override
    public Map<String, T> valuesMap() {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();
        if (repo instanceof AggregationRepositoryWithValuesMap) {
            Map<String, Exchange> exchangeMap = ((AggregationRepositoryWithValuesMap) repo).values(camelContext);
            Map<String, T> result = new HashMap<>(exchangeMap.size(), 1);
            for (Map.Entry<String, Exchange> entry : exchangeMap.entrySet()) {
                result.put(entry.getKey(), deserialize(entry.getValue()));
            }
            return result;
        }

        return AggregatorRepository.super.valuesMap();
    }

    @Override
    public Set<String> localKeys() {
        final AggregationRepository repo = plugin.getContext().getAggregationRepo();
        return (repo instanceof AggregationRepositoryWithLocalKeys)
               ? ((AggregationRepositoryWithLocalKeys) repo).localKeys()
               : repo.getKeys();
    }

    @SuppressWarnings("unchecked")
    private T deserialize(Exchange exchange) {
        ClassLoader realClassLoader = plugin.getContext().getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader thisClassLoader = getClass().getClassLoader();

        if (realClassLoader != thisClassLoader && contextClassLoader != thisClassLoader) {
            realClassLoader = contextClassLoader;
        }

        if (exchange != null) {
            return (T) plugin.getContext().getMessagesSerializer().deserialize(
                    exchange.getIn().getBody(), realClassLoader);
        }
        return null;
    }
}
