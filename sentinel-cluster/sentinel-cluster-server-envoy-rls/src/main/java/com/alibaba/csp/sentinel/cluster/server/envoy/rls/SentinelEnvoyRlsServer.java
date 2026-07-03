/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.cluster.server.envoy.rls;

import java.util.Optional;

import com.alibaba.csp.sentinel.cluster.server.envoy.rls.datasource.EnvoyRlsRuleDataSourceService;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author Eric Zhao
 */
public class SentinelEnvoyRlsServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("project.name", SentinelEnvoyRlsConstants.SERVER_APP_NAME);

        EnvoyRlsRuleDataSourceService dataSourceService = new EnvoyRlsRuleDataSourceService();
        dataSourceService.init();

        int port = resolvePort();
        SentinelRlsGrpcServer server = new SentinelRlsGrpcServer(port);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("[SentinelEnvoyRlsServer] Shutting down gRPC RLS server since JVM is shutting down");
            server.shutdown();
            dataSourceService.onShutdown();
            System.err.println("[SentinelEnvoyRlsServer] Server has been shut down");
        }));
        InitExecutor.doInit();

        server.blockUntilShutdown();
    }

    private static int resolvePort() {
        final int defaultPort = SentinelEnvoyRlsConstants.DEFAULT_GRPC_PORT;
        // Order: system env > property
        String portStr = Optional.ofNullable(System.getenv(SentinelEnvoyRlsConstants.GRPC_PORT_ENV_KEY))
            .orElse(SentinelConfig.getConfig(SentinelEnvoyRlsConstants.GRPC_PORT_PROPERTY_KEY));
        if (StringUtil.isBlank(portStr)) {
            return defaultPort;
        }
        try {
            int port = Integer.parseInt(portStr);
            if (port <= 0 || port > 65535) {
                RecordLog.warn("[SentinelEnvoyRlsServer] Invalid port <" + portStr + ">, using default" + defaultPort);
                return defaultPort;
            }
            return port;
        } catch (Exception ex) {
            RecordLog.warn("[SentinelEnvoyRlsServer] Failed to resolve port, using default " + defaultPort);
            System.err.println("[SentinelEnvoyRlsServer] Failed to resolve port, using default " + defaultPort);
            return defaultPort;
        }
    }
}
