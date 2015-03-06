package ru.yandex.qatools.camelot.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PluginWeb", propOrder = {
        "context"
})
public class PluginWeb extends Plugin {

    @JsonIgnore
    protected PluginWebContext context;

    public PluginWeb() {
    }

    public PluginWeb(Plugin other, PluginWebContext context) {
        this.id = other.id;
        this.aggregator = other.aggregator;
        this.baseInputUri = other.baseInputUri;
        this.baseOutputUri = other.baseOutputUri;
        this.brokerConfig = other.brokerConfig;
        this.processor = other.processor;
        this.resource = other.resource;
        this.source = other.source;
        this.context = context;
    }

    @Override
    public PluginWebContext getContext() {
        return context;
    }

    public void setContext(PluginWebContext context) {
        this.context = context;
    }
}
