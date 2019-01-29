/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.connection;

import java.util.List;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.log.RecordLog;

/**
 * @author xuyue
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ScanIdleConnectionTask implements Runnable {

    private final ConnectionPool connectionPool;

    public ScanIdleConnectionTask(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public void run() {
        try {
            int idleSeconds = ClusterServerConfigManager.getIdleSeconds();
            long idleTimeMillis = idleSeconds * 1000;
            if (idleTimeMillis < 0) {
                idleTimeMillis = ServerTransportConfig.DEFAULT_IDLE_SECONDS * 1000;
            }
            long now = System.currentTimeMillis();
            List<Connection> connections = connectionPool.listAllConnection();
            for (Connection conn : connections) {
                if ((now - conn.getLastReadTime()) > idleTimeMillis) {
                    RecordLog.info(
                        String.format("[ScanIdleConnectionTask] The connection <%s:%d> has been idle for <%d>s. "
                            + "It will be closed now.", conn.getRemoteIP(), conn.getRemotePort(), idleSeconds)
                    );
                    conn.close();
                }
            }
        } catch (Throwable t) {
            RecordLog.warn("[ScanIdleConnectionTask] Failed to clean-up idle tasks", t);
        }
    }
}
