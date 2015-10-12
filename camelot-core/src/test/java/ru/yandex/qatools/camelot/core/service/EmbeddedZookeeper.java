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

import org.apache.zookeeper.server.ServerCnxnFactory;
import org.apache.zookeeper.server.ZooKeeperServer;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import static org.apache.commons.io.FileUtils.deleteQuietly;
import static ru.yandex.qatools.camelot.util.FileUtil.createTempDirectory;

public class EmbeddedZookeeper {
    private int port = -1;
    private int tickTime = 500;

    private ServerCnxnFactory factory;
    private File snapshotDir;
    private File logDir;
    private String host;

    public EmbeddedZookeeper(int port) {
        this(port, 500);
    }

    public EmbeddedZookeeper(int port, int tickTime) {
        this.port = port;
        this.tickTime = tickTime;
        this.host = "localhost";
    }

    public void start() throws IOException {
        this.factory = ServerCnxnFactory.createFactory(new InetSocketAddress(host, port), 1024);
        this.snapshotDir = createTempDirectory();
        this.logDir = createTempDirectory();

        try {
            factory.startup(new ZooKeeperServer(snapshotDir, logDir, tickTime));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public void stop() {
        factory.shutdown();
        deleteQuietly(snapshotDir);
        deleteQuietly(logDir);
    }

    public String getHosts() {
        return host + ":" + getPort();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTickTime() {
        return tickTime;
    }

    public void setTickTime(int tickTime) {
        this.tickTime = tickTime;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EmbeddedZookeeper{");
        sb.append("connection=").append(host +":"+ port);
        sb.append('}');
        return sb.toString();
    }
}