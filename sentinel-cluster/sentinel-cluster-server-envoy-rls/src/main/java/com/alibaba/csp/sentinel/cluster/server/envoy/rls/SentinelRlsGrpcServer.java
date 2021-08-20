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

import java.io.IOException;

import com.alibaba.csp.sentinel.log.RecordLog;

import io.grpc.Server;
import io.grpc.ServerBuilder;

/**
 * @author Eric Zhao
 */
public class SentinelRlsGrpcServer {

    private final Server server;

    public SentinelRlsGrpcServer(int port) {
        ServerBuilder<?> builder = ServerBuilder.forPort(port)
            .addService(new com.alibaba.csp.sentinel.cluster.server.envoy.rls.service.v3.SentinelEnvoyRlsServiceImpl())
            .addService(new SentinelEnvoyRlsServiceImpl());

        server = builder.build();
    }

    public void start() throws IOException {
        // The gRPC server has already checked the start status, so we don't check here.
        server.start();
        String message = "[SentinelRlsGrpcServer] RLS server is running at port " + server.getPort();
        RecordLog.info(message);
        System.out.println(message);
    }

    public void shutdown() {
        server.shutdownNow();
    }

    public boolean isShutdown() {
        return server.isShutdown();
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
