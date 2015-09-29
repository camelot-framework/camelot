package ru.yandex.qatools.camelot.core.builders;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.AggregationRepository;
import ru.yandex.qatools.camelot.common.builders.MemoryAggregationRepositoryBuilder;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.core.impl.UnreachableAggregationRepository;

/**
 * @author Ilya Sadykov
 */
public class UnreachableAggregationRepositoryBuilder extends MemoryAggregationRepositoryBuilder {
    public UnreachableAggregationRepositoryBuilder(CamelContext camelContext, long waitForLockSec) {
        super(camelContext, waitForLockSec);
    }

    @Override
    public AggregationRepository initWritable(Plugin plugin) throws Exception { //NOSONAR
        return new UnreachableAggregationRepository();
    }
}
