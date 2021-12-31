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
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>gRPC server interceptor for Sentinel. Currently it only works with unary methods.</p>
 * <p>
 * Example code:
 * <pre>
 * Server server = ServerBuilder.forPort(port)
 *      .addService(new MyServiceImpl()) // Add your service.
 *      .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.
 *      .build();
 * </pre>
 * <p>
 * For client interceptor, see {@link SentinelGrpcClientInterceptor}.
 *
 * @author Eric Zhao
 */
public class SentinelGrpcServerInterceptor implements ServerInterceptor {
    private static final Status FLOW_CONTROL_BLOCK = Status.UNAVAILABLE.withDescription(
            "Flow control limit exceeded (server side)");
    private static final StatusRuntimeException STATUS_RUNTIME_EXCEPTION = new StatusRuntimeException(Status.CANCELLED);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        // Remote address: serverCall.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        Entry entry = null;
        try {
            entry = SphU.asyncEntry(fullMethodName, EntryType.IN);
            final AtomicReference<Entry> atomicReferenceEntry = new AtomicReference<>(entry);
            // Allow access, forward the call.
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
                    next.startCall(
                            new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
                                @Override
                                public void close(Status status, Metadata trailers) {
                                    Entry entry = atomicReferenceEntry.get();
                                    if (entry != null) {
                                        // Record the exception metrics.
                                        if (!status.isOk()) {
                                            Tracer.traceEntry(status.asRuntimeException(), entry);
                                        }
                                        //entry exit when the call be closed
                                        entry.exit();
                                    }
                                    super.close(status, trailers);
                                }
                            }, headers)) {
                /**
                 * If call was canceled, onCancel will be called. and the close will not be called
                 * so the server is encouraged to abort processing to save resources by onCancel
                 * @see ServerCall.Listener#onCancel()
                 */
                @Override
                public void onCancel() {
                    Entry entry = atomicReferenceEntry.get();
                    if (entry != null) {
                        Tracer.traceEntry(STATUS_RUNTIME_EXCEPTION, entry);
                        entry.exit();
                        atomicReferenceEntry.set(null);
                    }
                    super.onCancel();
                }
            };
        } catch (BlockException e) {
            call.close(FLOW_CONTROL_BLOCK, new Metadata());
            return new ServerCall.Listener<ReqT>() {
            };
        } catch (RuntimeException e) {
            // Catch the RuntimeException startCall throws, entry is guaranteed to exit.
            if (entry != null) {
                Tracer.traceEntry(e, entry);
                entry.exit();
            }
            throw e;
        }
    }
}
