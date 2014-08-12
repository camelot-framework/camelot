package ru.yandex.qatools.camelot.beans;

import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.core.SplitStrategy;

import static java.util.Arrays.copyOf;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public class RouteConfigImpl implements RouteConfig {
    private SplitStrategy splitStrategy = null;
    private Class[] filterInstanceOf = null;
    private Class<? extends CustomFilter> customFilter = null;

    @Override
    public Class[] getFilterInstanceOf() {
        return filterInstanceOf;
    }

    @Override
    public Class<? extends CustomFilter> getCustomFilter() {
        return customFilter;
    }

    @Override
    public SplitStrategy getSplitStrategy() {
        return splitStrategy;
    }

    public void setSplitStrategy(SplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
    }

    public void setFilterInstanceOf(Class[] filterClass) {
        this.filterInstanceOf = (filterClass != null) ? copyOf(filterClass, filterClass.length) : new Class[0];
    }

    public void setCustomFilter(Class<? extends CustomFilter> customFilter) {
        this.customFilter = customFilter;
    }
}
