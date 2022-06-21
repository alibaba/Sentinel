package com.alibaba.csp.sentinel.adapter.grpc.extractor;

import com.alibaba.csp.sentinel.spi.SpiLoader;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;

import java.util.Objects;

/**
 * Avoid separation of logic
 *
 * @author howie
 */
public class GrpcResourceExtractorManager {

    private static GrpcResourceExtractor INSTANCE = new DefaultGrpcResourceExtractor();

    static {
        GrpcResourceExtractor customerInstance = SpiLoader.of(GrpcResourceExtractor.class).loadFirstInstance();
        if (Objects.nonNull(customerInstance)) {
            INSTANCE = customerInstance;
        }
    }

    /**
     * Extracts the resource name from the methodDescriptor
     *
     * @param methodDescriptor {@link io.grpc.ClientInterceptor} or {@link ServerCall#getMethodDescriptor()}
     * @param <ReqT>           request param
     * @param <RespT>          response param
     * @return
     */
    public static <ReqT, RespT> String extract(MethodDescriptor<ReqT, RespT> methodDescriptor) {
        return INSTANCE.extract(methodDescriptor);
    }
}
