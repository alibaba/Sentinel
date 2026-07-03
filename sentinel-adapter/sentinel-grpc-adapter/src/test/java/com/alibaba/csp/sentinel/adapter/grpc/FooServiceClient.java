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
package com.alibaba.csp.sentinel.adapter.grpc;

import com.alibaba.csp.sentinel.adapter.grpc.gen.FooRequest;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooResponse;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooServiceGrpc;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * A simple wrapped gRPC client for FooService.
 *
 * @author Eric Zhao
 */
final class FooServiceClient {
    private final ManagedChannel channel;
    private final FooServiceGrpc.FooServiceBlockingStub blockingStub;

    FooServiceClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.blockingStub = FooServiceGrpc.newBlockingStub(this.channel);
    }

    FooServiceClient(String host, int port, ClientInterceptor interceptor) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(interceptor)
                .build();
        this.blockingStub = FooServiceGrpc.newBlockingStub(this.channel);
    }

    FooResponse sayHello(FooRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        return blockingStub.sayHello(request);
    }

    FooResponse anotherHello(FooRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        return blockingStub.anotherHello(request);
    }

    void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
    }
}
