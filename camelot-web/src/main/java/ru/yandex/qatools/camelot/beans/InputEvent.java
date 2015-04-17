package ru.yandex.qatools.camelot.beans;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InputEvent", propOrder = {
        "event",
})
public class InputEvent implements Serializable {

    @XmlElement(name = "event")
    protected Object event;

    public void setEvent(Object event) {
        this.event = event;
    }

    public Object getEvent() {
        return event;

    }
}
