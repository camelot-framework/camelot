package ru.yandex.qatools.camelot.api;

/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
public final class Constants {

    Constants() {
    }

    public static final class Keys {

        public static final String ALL = "all";

        Keys() {
        }
    }

    public static final class Headers {

        public static final String CORRELATION_KEY = "correlationKey";
        public static final String LABEL = "label";
        public static final String UUID = "uuid";
        public static final String PLUGIN_ID = "pluginId";
        public static final String TOPIC = "topic";
        public static final String FINISHED_EXCHANGE = "finishedExchange";
        public static final String BODY_CLASS = "bodyClass";

        Headers() {
        }
    }
}
