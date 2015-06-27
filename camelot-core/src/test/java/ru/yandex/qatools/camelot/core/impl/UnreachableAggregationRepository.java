package ru.yandex.qatools.camelot.core.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.api.error.RepositoryUnreachableException;

import java.util.Collections;
import java.util.Set;

/**
 * @author Ilya Sadykov
 */
public class UnreachableAggregationRepository implements AggregationRepository {
    @Override
    public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
        throw new RepositoryUnreachableException("Failed to add");
    }

    @Override
    public Exchange get(CamelContext camelContext, String key) {
        throw new RepositoryUnreachableException("Failed to get");
    }

    @Override
    public void remove(CamelContext camelContext, String key, Exchange exchange) {
        throw new RepositoryUnreachableException("Failed to remove");
    }

    @Override
    public void confirm(CamelContext camelContext, String exchangeId) {
        throw new RepositoryUnreachableException("Failed to confirm");
    }

    @Override
    public Set<String> getKeys() {
        return Collections.emptySet();
    }
}
