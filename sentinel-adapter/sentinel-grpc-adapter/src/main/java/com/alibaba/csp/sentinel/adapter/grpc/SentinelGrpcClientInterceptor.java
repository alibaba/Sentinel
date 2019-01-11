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

import javax.annotation.Nullable;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

/**
 * <p>gRPC client interceptor for Sentinel. Currently it only works with unary methods.</p>
 *
 * Example code:
 * <pre>
 * public class ServiceClient {
 *
 *     private final ManagedChannel channel;
 *
 *     ServiceClient(String host, int port) {
 *         this.channel = ManagedChannelBuilder.forAddress(host, port)
 *             .intercept(new SentinelGrpcClientInterceptor()) // Add the client interceptor.
 *             .build();
 *         // Init your stub here.
 *     }
 *
 * }
 * </pre>
 *
 * For server interceptor, see {@link SentinelGrpcServerInterceptor}.
 *
 * @author Eric Zhao
 */
public class SentinelGrpcClientInterceptor implements ClientInterceptor {

    private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE.withDescription(
        "Flow control limit exceeded (client side)");

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions, Channel channel) {
        String resourceName = methodDescriptor.getFullMethodName();
        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.OUT);
            // Allow access, forward the call.
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
                channel.newCall(methodDescriptor, callOptions)) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    super.start(new SimpleForwardingClientCallListener<RespT>(responseListener) {
                        @Override
                        public void onReady() {
                            super.onReady();
                        }

                        @Override
                        public void onClose(Status status, Metadata trailers) {
                            super.onClose(status, trailers);
                            // Record the exception metrics.
                            if (!status.isOk()) {
                                recordException(status.asRuntimeException());
                            }
                        }
                    }, headers);
                }

                @Override
                public void cancel(@Nullable String message, @Nullable Throwable cause) {
                    super.cancel(message, cause);
                    // Record the exception metrics.
                    recordException(cause);
                }
            };
        } catch (BlockException e) {
            // Flow control threshold exceeded, block the call.
            return new ClientCall<ReqT, RespT>() {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    responseListener.onClose(FLOW_CONTROL_BLOCK, new Metadata());
                }

                @Override
                public void request(int numMessages) {

                }

                @Override
                public void cancel(@Nullable String message, @Nullable Throwable cause) {

                }

                @Override
                public void halfClose() {

                }

                @Override
                public void sendMessage(ReqT message) {

                }
            };
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    private void recordException(Throwable t) {
        Tracer.trace(t);
    }
}
