package ru.yandex.qatools.camelot.core.beans;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author: Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public interface FailureDescription {
    @XmlElement(required = true)
    public String getType();

    public String getMessage();

    @XmlElement(required = true)
    public String getStackTrace();

}
