package ru.yandex.qatools.camelot.mongodb;

import com.mongodb.MongoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.mongodb.MongoPessimisticLocking;
import ru.qatools.mongodb.MongoPessimisticRepo;
import ru.yandex.qatools.camelot.api.annotations.OnInit;
import ru.yandex.qatools.camelot.common.PluginAnnotatedMethodInvoker;
import ru.yandex.qatools.camelot.common.PluginInitializer;
import ru.yandex.qatools.camelot.config.Plugin;
import ru.yandex.qatools.camelot.util.ReflectUtil;


/**
 * @author Ilya Sadykov (mailto: smecsia@yandex-team.ru)
 */
@SuppressWarnings("Duplicates")
public class MongodbPluginInitializer implements PluginInitializer {
    public static final String PLUGIN_INIT_KS = "_plugin_";
    public static final int PLUGIN_INIT_MAX_INTERVAL_MS = 500;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final MongoPessimisticRepo mongoRepo;

    public MongodbPluginInitializer(MongoClient mongoClient, String dbName) {
        this.mongoRepo = new MongoPessimisticRepo(
                new MongoPessimisticLocking(mongoClient, dbName, PLUGIN_INIT_KS, PLUGIN_INIT_MAX_INTERVAL_MS)
        );
    }

    public void init(Plugin plugin) throws Exception { //NOSONAR
        new PluginAnnotatedMethodInvoker<>(plugin, OnInit.class).process((method, annotation) -> {
            try {
                return !(boolean) ReflectUtil.getAnnotationValue(annotation, "synchronous");
            } catch (Exception e) {
                LOGGER.debug("Failed to read annotation value", e);
            }
            return false;
        }).invoke();

        new MongodbSyncAnnotatedMethodInvoker<>(mongoRepo, plugin, OnInit.class, 30, true).process(
                (method, annotation) -> {
                    try {
                        return (boolean) ReflectUtil.getAnnotationValue(annotation, "synchronous");
                    } catch (Exception e) {
                        LOGGER.debug("Failed to read annotation value", e);
                    }
                    return false;
                }).invoke();
    }
}
