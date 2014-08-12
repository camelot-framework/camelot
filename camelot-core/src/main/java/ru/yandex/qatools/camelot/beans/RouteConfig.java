package ru.yandex.qatools.camelot.beans;

import ru.yandex.qatools.camelot.api.CustomFilter;
import ru.yandex.qatools.camelot.core.SplitStrategy;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface RouteConfig {
    Class[] getFilterInstanceOf();

    Class<? extends CustomFilter> getCustomFilter();

    SplitStrategy getSplitStrategy();
}
