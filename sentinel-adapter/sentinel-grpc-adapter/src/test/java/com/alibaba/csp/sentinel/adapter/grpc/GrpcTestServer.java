package com.alibaba.csp.sentinel.adapter.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

class GrpcTestServer {
    private Server server;

    GrpcTestServer() {
    }

    void start(int port, boolean shouldintercept) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server already running!");
        }
        ServerBuilder<?> serverBuild = ServerBuilder.forPort(port)
                .addService(new FooServiceImpl());
        if (shouldintercept) {
            serverBuild.intercept(new SentinelGrpcServerInterceptor());
        }
        server = serverBuild.build();
        server.start();
    }

    void stop() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}