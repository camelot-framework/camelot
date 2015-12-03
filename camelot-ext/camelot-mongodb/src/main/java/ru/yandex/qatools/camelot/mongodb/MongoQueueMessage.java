package ru.yandex.qatools.camelot.mongodb;

import java.io.Serializable;

public class MongoQueueMessage implements Serializable {
    final Serializable object;
    final String topic;
    final String pluginId;

    public MongoQueueMessage(String pluginId, Object object) {
        this(pluginId, object, null);
    }

    public MongoQueueMessage(String pluginId, Object object, String topic) {
        if (!(object instanceof Serializable)) {
            throw new RuntimeException("Could not send message '" + object + "': it's not serializable!");//NOSONAR
        }
        this.pluginId = pluginId;
        this.object = (Serializable) object;
        this.topic = topic;
    }
}
