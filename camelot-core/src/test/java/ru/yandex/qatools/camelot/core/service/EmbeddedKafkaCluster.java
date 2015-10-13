/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.yandex.qatools.camelot.core.service;

import kafka.admin.AdminUtils;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import org.I0Itec.zkclient.ZkClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static ru.yandex.qatools.camelot.util.FileUtil.createTempDirectory;

public class EmbeddedKafkaCluster {
    private final int port;
    private final String zkHosts;
    private final Properties baseProperties;

    private final String brokerList;

    private final List<KafkaServer> brokers;
    private final List<File> logDirs;

    public EmbeddedKafkaCluster(String zkHosts, int port) {
        this.zkHosts = zkHosts;
        this.port = port;
        this.baseProperties = new Properties();
        this.brokers = new ArrayList<>();
        this.logDirs = new ArrayList<>();
        this.brokerList = constructBrokerList(port);
    }

    public String allUri(String topic) {
        return format("kafka:localhost:%s?groupId=all&topic=%s&zookeeperConnect=%s",
                port, topic, zkHosts);
    }

    public ZkClient getZkClient() {
        for (KafkaServer server : brokers) {
            return server.zkClient();
        }
        return null;
    }

    public void createTopics(String... topics) {
        for (String topic : topics) {
            AdminUtils.createTopic(getZkClient(), topic, 2, 1, new Properties());
        }
    }


    private String constructBrokerList(int port) {
        StringBuilder sb = new StringBuilder();
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append("localhost:").append(port);
        return sb.toString();
    }

    public void start() throws IOException {
        File logDir = createTempDirectory();

        Properties properties = new Properties();
        properties.putAll(baseProperties);
        properties.setProperty("zookeeper.connect", zkHosts);
        properties.setProperty("broker.id", String.valueOf(0));
        properties.setProperty("host.name", "localhost");
        properties.setProperty("port", Integer.toString(port));
        properties.setProperty("log.dir", logDir.getAbsolutePath());
        properties.setProperty("num.partitions", String.valueOf(1));
        properties.setProperty("auto.create.topics.enable", String.valueOf(Boolean.TRUE));
        System.out.println("EmbeddedKafkaCluster: local directory: " + logDir.getAbsolutePath());
        properties.setProperty("log.flush.interval.messages", String.valueOf(1));

        KafkaServer broker = startBroker(properties);

        brokers.add(broker);
        logDirs.add(logDir);
    }


    private KafkaServer startBroker(Properties props) {
        KafkaServer server = new KafkaServer(new KafkaConfig(props), new SystemTime());
        server.startup();
        return server;
    }

    public String getBrokerList() {
        return brokerList;
    }

    public int getPort() {
        return port;
    }

    public void stop() {
        for (KafkaServer broker : brokers) {
            try {
                broker.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (File logDir : logDirs) {
            deleteQuietly(logDir);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmbeddedKafkaCluster{");
        sb.append("brokerList='").append(brokerList).append('\'');
        sb.append('}');
        return sb.toString();
    }
}