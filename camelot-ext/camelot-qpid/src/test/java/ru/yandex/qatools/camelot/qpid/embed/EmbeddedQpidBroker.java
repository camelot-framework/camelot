package ru.yandex.qatools.camelot.qpid.embed;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

import static java.nio.file.Files.createTempDirectory;
import static ru.yandex.qatools.camelot.util.SocketUtil.findFreePort;

/**
 * @author Ilya Sadykov
 */
public class EmbeddedQpidBroker {
    final static Logger LOGGER = LoggerFactory.getLogger(EmbeddedQpidBroker.class);
    final Path tmpFolder;
    final String qpidHomeDir = "target/test-classes";
    final String configFileName = "/qpid-config.json";
    final int amqpPort = findFreePort();
    private Broker broker;

    public EmbeddedQpidBroker() throws Exception {
        tmpFolder = createTempDirectory("qpid-embedded");
        broker = new Broker();
        BrokerOptions brokerOptions = new BrokerOptions();

        final File file = new File(qpidHomeDir);
        String homePath = file.getAbsolutePath();
        LOGGER.info(" qpid home dir=" + homePath);
        LOGGER.info(" qpid work dir=" + tmpFolder.toAbsolutePath());

        brokerOptions.setConfigProperty("qpid.work_dir", String.valueOf(tmpFolder.toAbsolutePath()));
        brokerOptions.setConfigProperty("qpid.amqp_port", String.valueOf(amqpPort));
        brokerOptions.setConfigProperty("qpid.home_dir", homePath);
        brokerOptions.setConfigProperty("qpid.password_file", homePath + "/passwd");
        brokerOptions.setInitialConfigurationLocation(homePath + configFileName);
        broker.startup(brokerOptions);
        LOGGER.info("broker started");
    }

    public int getAmqpPort() {
        return amqpPort;
    }

    public void stop() {
        broker.shutdown(0);
    }
}
