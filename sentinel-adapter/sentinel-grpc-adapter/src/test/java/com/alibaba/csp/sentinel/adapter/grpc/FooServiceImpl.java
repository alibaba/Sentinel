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

import io.grpc.stub.StreamObserver;

/**
 * Implementation of FooService defined in proto.
 */
class FooServiceImpl extends FooServiceGrpc.FooServiceImplBase {

    @Override
    public void sayHello(FooRequest request, StreamObserver<FooResponse> responseObserver) {
        String message = String.format("Hello %s! Your ID is %d.", request.getName(), request.getId());
        FooResponse response = FooResponse.newBuilder().setMessage(message).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void anotherHello(FooRequest request, StreamObserver<FooResponse> responseObserver) {
        String message = String.format("Good day, %s (%d)", request.getName(), request.getId());
        FooResponse response = FooResponse.newBuilder().setMessage(message).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
