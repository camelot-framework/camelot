package ru.yandex.qatools.camelot.core.beans;

public class ConfigUtils {

    public static <T extends BaseConfig> T findConfig(Config base, Class<T> config) {
        for (BaseConfig c : base.getConfigs()) {
            if (config.isInstance(c) || config.getName().equals(c.getClass().getName())) {
                return (T) c;
            }
        }
        return null;
    }

    public static Config newConfig(BaseConfig config) {
        Config res = new Config();
        res.getConfigs().add(config);
        return res;
    }
}
