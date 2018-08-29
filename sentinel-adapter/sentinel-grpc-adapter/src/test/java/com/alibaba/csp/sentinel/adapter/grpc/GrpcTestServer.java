package com.alibaba.csp.sentinel.adapter.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

class GrpcTestServer {
    private Server server;

    GrpcTestServer() {
    }

    void prepare(int port, boolean shouldIntercept) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server already running!");
        }
        server = ServerBuilder.forPort(port)
                .addService(new FooServiceImpl());
        if(shouldIntercept)
            server.intercept(new SentinelGrpcServerInterceptor());
        server.build();
        server.start();
    }

    void stop() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}