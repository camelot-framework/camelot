package ru.yandex.qatools.camelot.core.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public interface TestEventDescription {
    @XmlElement(required = true)
    TestEventData getEventData();

    @XmlElement(required = true)
    FailureDescription getFailureInfo();

    @XmlElement(name = "meta")
    List<MetaData> getMetas();

    @XmlElement(name = "testResult")
    Notification getTestResult();

}
