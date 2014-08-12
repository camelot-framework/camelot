package ru.yandex.qatools.camelot.core.builders;

import ru.yandex.qatools.camelot.config.Plugin;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface SchedulerBuildersFactory {

    SchedulerBuilder build(Plugin plugin);

}
