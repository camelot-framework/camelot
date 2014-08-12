package ru.yandex.qatools.camelot.beans;

/**
 * @author smecsia
 */
public class AggregationOptionsImpl implements AggregationOptions {

    private final boolean useOptimisticLocking;

    public AggregationOptionsImpl(boolean useOptimisticLocking) {
        this.useOptimisticLocking = useOptimisticLocking;
    }

    @Override
    public boolean useOptimisticLocking() {
        return useOptimisticLocking;
    }
}
