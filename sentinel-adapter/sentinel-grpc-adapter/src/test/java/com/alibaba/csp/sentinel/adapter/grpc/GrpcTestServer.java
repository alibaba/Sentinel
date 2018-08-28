package com.alibaba.csp.sentinel.adapter.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

import static org.junit.Assert.*;

class GrpcTestServer {
    private Server server;

    GrpcTestServer() {
    }

    void prepare(int port) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server already running!");
        }
        server = ServerBuilder.forPort(port)
                .addService(new FooServiceImpl())
                .intercept(new SentinelGrpcServerInterceptor())
                .build();
        server.start();
    }

    void stop() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}