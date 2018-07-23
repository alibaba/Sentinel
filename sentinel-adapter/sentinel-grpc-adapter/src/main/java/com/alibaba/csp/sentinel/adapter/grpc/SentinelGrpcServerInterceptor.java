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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;

/**
 * <p>gRPC server interceptor for Sentinel. Currently it only works with unary methods.</p>
 *
 * Example code:
 * <pre>
 * Server server = ServerBuilder.forPort(port)
 *      .addService(new MyServiceImpl()) // Add your service.
 *      .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.
 *      .build();
 * </pre>
 *
 * For client interceptor, see {@link SentinelGrpcClientInterceptor}.
 *
 * @author Eric Zhao
 */
public class SentinelGrpcServerInterceptor implements ServerInterceptor {

    private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE.withDescription(
        "Flow control limit exceeded (server side)");

    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata,
                                                      ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String resourceName = serverCall.getMethodDescriptor().getFullMethodName();
        // Remote address: serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        Entry entry = null;
        try {
            ContextUtil.enter(resourceName);
            entry = SphU.entry(resourceName, EntryType.IN);
            // Allow access, forward the call.
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                serverCallHandler.startCall(
                    new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
                        @Override
                        public void close(Status status, Metadata trailers) {
                            super.close(status, trailers);
                            // Record the exception metrics.
                            if (!status.isOk()) {
                                recordException(status.asRuntimeException());
                            }
                        }
                    }, metadata)) {};
        } catch (BlockException e) {
            serverCall.close(FLOW_CONTROL_BLOCK, new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    private void recordException(Throwable t) {
        Tracer.trace(t);
    }
}
