package com.alibaba.csp.sentinel.adapter.grpc.extractor;

import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

/**
 * MethodDescriptor to resource name
 *
 * @author howie
 * @since 1.8.4
 */
public interface GrpcResourceExtractor {

    /**
     * Extracts the resource name from the methodDescriptor
     *
     * @param methodDescriptor {@link io.grpc.ClientInterceptor} or {@link ServerCall#getMethodDescriptor()}
     * @param <ReqT>           request param
     * @param <RespT>          response param
     * @return
     */
    <ReqT, RespT> String extract(MethodDescriptor<ReqT, RespT> methodDescriptor);
}
