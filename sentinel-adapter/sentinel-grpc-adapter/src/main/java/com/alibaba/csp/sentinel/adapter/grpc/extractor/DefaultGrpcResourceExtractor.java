package com.alibaba.csp.sentinel.adapter.grpc.extractor;

import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

/**
 * Default implementation
 *
 * @author howie
 */
public class DefaultGrpcResourceExtractor implements GrpcResourceExtractor {

    /**
     * Extracts the resource name from the methodDescriptor
     *
     * @param methodDescriptor {@link ClientInterceptor} or {@link ServerCall#getMethodDescriptor()}
     * @return
     */
    @Override
    public <ReqT, RespT> String extract(MethodDescriptor<ReqT, RespT> methodDescriptor) {
        return methodDescriptor.getFullMethodName();
    }
}
