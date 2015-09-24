package ru.yandex.qatools.camelot.common.builders;

import org.apache.camel.Processor;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface ProcessorRoutesBuilder extends BasicRoutesBuilder {

    void setProcessor(Processor processor);

    Processor getProcessor();

}
