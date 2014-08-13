package ru.yandex.qatools.camelot.core.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public interface MetaData {

    @XmlElement(required = true)
    public String getKey();

    @XmlElement(required = true)
    public String getName();

    @XmlElement(required = true)
    public String getValue();
}
