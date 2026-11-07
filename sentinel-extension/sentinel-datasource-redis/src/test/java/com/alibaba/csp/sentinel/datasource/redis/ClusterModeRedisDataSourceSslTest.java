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

package com.alibaba.csp.sentinel.datasource.redis;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.alibaba.csp.sentinel.datasource.redis.config.RedisConnectionConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Checks that a cluster mode data source with SSL enabled starts a TLS handshake with the cluster nodes.
 */
public class ClusterModeRedisDataSourceSslTest {

    /**
     * First byte of a TLS record carrying a handshake message (ClientHello).
     */
    private static final int TLS_HANDSHAKE_RECORD = 0x16;

    @Test(timeout = 30000)
    public void testClusterNodeConnectionUsesSslWhenEnabled() throws Exception {
        try (ServerSocket server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
            CompletableFuture<Integer> firstByte = new CompletableFuture<>();
            Thread acceptor = new Thread(() -> {
                while (!server.isClosed()) {
                    try (Socket socket = server.accept()) {
                        InputStream in = socket.getInputStream();
                        firstByte.complete(in.read());
                    } catch (IOException e) {
                        // server closed
                    }
                }
            });
            acceptor.setDaemon(true);
            acceptor.start();

            RedisConnectionConfig config = RedisConnectionConfig.builder()
                .withRedisCluster(server.getInetAddress().getHostAddress(), server.getLocalPort())
                .withSslEnable(true)
                .withTimeout(2000)
                .build();
            try {
                new RedisDataSource<>(config, "sentinel.rules.flow.ruleKey", "sentinel.rules.flow.channel",
                    source -> source);
            } catch (Exception e) {
                // there is no Redis behind the socket
            }

            Assert.assertEquals(TLS_HANDSHAKE_RECORD, (int) firstByte.get(10, TimeUnit.SECONDS));
        }
    }
}
