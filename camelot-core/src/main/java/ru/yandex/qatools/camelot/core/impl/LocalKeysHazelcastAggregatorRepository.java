package ru.yandex.qatools.camelot.core.impl;

import ru.yandex.qatools.camelot.core.AggregationRepositoryWithLocalKeys;
import ru.yandex.qatools.camelot.hazelcast.HazelcastAggregationRepository;

import java.util.Set;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class LocalKeysHazelcastAggregatorRepository extends HazelcastAggregationRepository
        implements AggregationRepositoryWithLocalKeys {
    @Override
    public Set<String> localKeys() {
        try {
            return getMap().localKeySet();
        } catch (UnsupportedOperationException e) {
            return getMap().keySet();
        }
    }
}
