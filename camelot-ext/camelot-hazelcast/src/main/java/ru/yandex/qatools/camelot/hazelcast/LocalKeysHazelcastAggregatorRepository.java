package ru.yandex.qatools.camelot.hazelcast;


import ru.yandex.qatools.camelot.common.AggregationRepositoryWithLocalKeys;

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
        } catch (UnsupportedOperationException e) {//NOSONAR
            return getMap().keySet();
        }
    }
}
