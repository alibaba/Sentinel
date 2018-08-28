package com.alibaba.csp.sentinel.adapter.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

import static org.junit.Assert.*;

class ExtractedSentinelGrpcClientInterceptorTest {
    private Server server;

    ExtractedSentinelGrpcClientInterceptorTest() {
    }

    void prepareServer(int port) throws IOException {
        if (server != null) {
            throw new IllegalStateException("Server already running!");
        }
        server = ServerBuilder.forPort(port)
                .addService(new FooServiceImpl())
                .build();
        server.start();
    }

    void stopServer() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}