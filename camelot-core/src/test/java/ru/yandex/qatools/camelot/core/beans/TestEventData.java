package ru.yandex.qatools.camelot.core.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public interface TestEventData {
    @XmlElement(required = true)
    public long getTimestamp();

    @XmlAttribute(name = "time", required = true)
    public long getTime();

    @XmlElement(required = true)
    public String getSystem();

    @XmlElement(required = true)
    public String getProfile();

    @XmlElement(required = true)
    public String getPackagename();

    @XmlElement(required = true)
    public String getClassname();

    @XmlElement(required = true)
    public String getMethodname();
}
