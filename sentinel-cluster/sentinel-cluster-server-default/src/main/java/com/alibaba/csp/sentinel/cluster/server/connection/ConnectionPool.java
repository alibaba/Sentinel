/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.log.RecordLog;

import io.netty.channel.Channel;

/**
 * Universal connection pool for connection management.
 *
 * @author xuyue
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ConnectionPool {

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(2);

    /**
     * Format: ("ip:port", connection)
     */
    private final Map<String, Connection> CONNECTION_MAP = new ConcurrentHashMap<String, Connection>();

    /**
     * Periodic scan task.
     */
    private ScheduledFuture scanTaskFuture = null;

    public void createConnection(Channel channel) {
        if (channel != null) {
            Connection connection = new NettyConnection(channel, this);

            String connKey = getConnectionKey(channel);
            CONNECTION_MAP.put(connKey, connection);
        }
    }

    /**
     * Start the scan task for long-idle connections.
     */
    private synchronized void startScan() {
        if (scanTaskFuture == null
            || scanTaskFuture.isCancelled()
            || scanTaskFuture.isDone()) {
            scanTaskFuture = TIMER.scheduleAtFixedRate(
                new ScanIdleConnectionTask(this), 10, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Format to "ip:port".
     *
     * @param channel channel
     * @return formatted key
     */
    private String getConnectionKey(Channel channel) {
        InetSocketAddress socketAddress = (InetSocketAddress)channel.remoteAddress();
        String remoteIp = socketAddress.getAddress().getHostAddress();
        int remotePort = socketAddress.getPort();
        return remoteIp + ":" + remotePort;
    }

    private String getConnectionKey(String ip, int port) {
        return ip + ":" + port;
    }

    public void refreshLastReadTime(Channel channel) {
        if (channel != null) {
            String connKey = getConnectionKey(channel);
            Connection connection = CONNECTION_MAP.get(connKey);
            if (connection != null) {
                connection.refreshLastReadTime(System.currentTimeMillis());
            }
        }
    }

    public Connection getConnection(String remoteIp, int remotePort) {
        String connKey = getConnectionKey(remoteIp, remotePort);
        return CONNECTION_MAP.get(connKey);
    }

    public void remove(Channel channel) {
        String connKey = getConnectionKey(channel);
        CONNECTION_MAP.remove(connKey);
    }

    public List<Connection> listAllConnection() {
        List<Connection> connections = new ArrayList<Connection>(CONNECTION_MAP.values());
        return connections;
    }

    public int count() {
        return CONNECTION_MAP.size();
    }

    public void clear() {
        CONNECTION_MAP.clear();
    }

    public void shutdownAll() throws Exception {
        for (Connection c : CONNECTION_MAP.values()) {
            c.close();
        }
    }

    public void refreshIdleTask() {
        if (scanTaskFuture == null || scanTaskFuture.cancel(false)) {
            startScan();
        } else {
            RecordLog.info("The result of canceling scanTask is error.");
        }
    }
}

