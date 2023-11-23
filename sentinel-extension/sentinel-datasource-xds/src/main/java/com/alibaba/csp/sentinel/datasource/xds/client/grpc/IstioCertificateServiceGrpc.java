package com.alibaba.csp.sentinel.datasource.xds.client.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * Service for managing certificates issued by the CA.
 * </pre>
 */
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.34.1)",
    comments = "Source: ca.proto")
public final class IstioCertificateServiceGrpc {

    public static final String SERVICE_NAME = "istio.v1.auth.IstioCertificateService";
    private static final int METHODID_CREATE_CERTIFICATE = 0;
    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<IstioCertificateRequest, IstioCertificateResponse>
        getCreateCertificateMethod;
    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    private IstioCertificateServiceGrpc() {
    }

    @io.grpc.stub.annotations.RpcMethod(
        fullMethodName = SERVICE_NAME + '/' + "CreateCertificate",
        requestType = IstioCertificateRequest.class,
        responseType = IstioCertificateResponse.class,
        methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<IstioCertificateRequest, IstioCertificateResponse> getCreateCertificateMethod() {
        io.grpc.MethodDescriptor<IstioCertificateRequest, IstioCertificateResponse> getCreateCertificateMethod;
        if ((getCreateCertificateMethod = IstioCertificateServiceGrpc.getCreateCertificateMethod) == null) {
            synchronized (IstioCertificateServiceGrpc.class) {
                if ((getCreateCertificateMethod = IstioCertificateServiceGrpc.getCreateCertificateMethod) == null) {
                    IstioCertificateServiceGrpc.getCreateCertificateMethod = getCreateCertificateMethod
                        = io.grpc.MethodDescriptor.<IstioCertificateRequest, IstioCertificateResponse>newBuilder()
                        .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(generateFullMethodName(SERVICE_NAME,
                            "CreateCertificate"))
                        .setSampledToLocalTracing(true)
                        .setRequestMarshaller(io.grpc.protobuf.ProtoUtils
                            .marshaller(IstioCertificateRequest
                                .getDefaultInstance()))
                        .setResponseMarshaller(io.grpc.protobuf.ProtoUtils
                            .marshaller(IstioCertificateResponse
                                .getDefaultInstance()))
                        .setSchemaDescriptor(
                            new IstioCertificateServiceMethodDescriptorSupplier(
                                "CreateCertificate"))
                        .build();
                }
            }
        }
        return getCreateCertificateMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static IstioCertificateServiceStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceStub> factory
            = new io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceStub>() {
            @Override
            public IstioCertificateServiceStub newStub(io.grpc.Channel channel,
                                                       io.grpc.CallOptions callOptions) {
                return new IstioCertificateServiceStub(channel, callOptions);
            }
        };
        return IstioCertificateServiceStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on
     * the service
     */
    public static IstioCertificateServiceBlockingStub newBlockingStub(
        io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceBlockingStub> factory
            = new io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceBlockingStub>() {
            @Override
            public IstioCertificateServiceBlockingStub newStub(io.grpc.Channel channel,
                                                               io.grpc.CallOptions callOptions) {
                return new IstioCertificateServiceBlockingStub(channel, callOptions);
            }
        };
        return IstioCertificateServiceBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static IstioCertificateServiceFutureStub newFutureStub(
        io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceFutureStub> factory
            = new io.grpc.stub.AbstractStub.StubFactory<IstioCertificateServiceFutureStub>() {
            @Override
            public IstioCertificateServiceFutureStub newStub(io.grpc.Channel channel,
                                                             io.grpc.CallOptions callOptions) {
                return new IstioCertificateServiceFutureStub(channel, callOptions);
            }
        };
        return IstioCertificateServiceFutureStub.newStub(factory, channel);
    }

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (IstioCertificateServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor
                        .newBuilder(SERVICE_NAME)
                        .setSchemaDescriptor(
                            new IstioCertificateServiceFileDescriptorSupplier())
                        .addMethod(getCreateCertificateMethod()).build();
                }
            }
        }
        return result;
    }

    /**
     * <pre>
     * Service for managing certificates issued by the CA.
     * </pre>
     */
    public static abstract class IstioCertificateServiceImplBase
        implements io.grpc.BindableService {

        /**
         * <pre>
         * Using provided CSR, returns a signed certificate.
         * </pre>
         */
        public void createCertificate(IstioCertificateRequest request,
                                      io.grpc.stub.StreamObserver<IstioCertificateResponse> responseObserver) {
            asyncUnimplementedUnaryCall(getCreateCertificateMethod(), responseObserver);
        }

        @Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(getCreateCertificateMethod(), asyncUnaryCall(
                    new MethodHandlers<IstioCertificateRequest, IstioCertificateResponse>(
                        this, METHODID_CREATE_CERTIFICATE)))
                .build();
        }

    }

    /**
     * <pre>
     * Service for managing certificates issued by the CA.
     * </pre>
     */
    public static final class IstioCertificateServiceStub
        extends io.grpc.stub.AbstractAsyncStub<IstioCertificateServiceStub> {

        private IstioCertificateServiceStub(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IstioCertificateServiceStub build(io.grpc.Channel channel,
                                                    io.grpc.CallOptions callOptions) {
            return new IstioCertificateServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         * Using provided CSR, returns a signed certificate.
         * </pre>
         */
        public void createCertificate(IstioCertificateRequest request,
                                      io.grpc.stub.StreamObserver<IstioCertificateResponse> responseObserver) {
            asyncUnaryCall(
                getChannel().newCall(getCreateCertificateMethod(), getCallOptions()),
                request, responseObserver);
        }

    }

    /**
     * <pre>
     * Service for managing certificates issued by the CA.
     * </pre>
     */
    public static final class IstioCertificateServiceBlockingStub extends
        io.grpc.stub.AbstractBlockingStub<IstioCertificateServiceBlockingStub> {

        private IstioCertificateServiceBlockingStub(io.grpc.Channel channel,
                                                    io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IstioCertificateServiceBlockingStub build(io.grpc.Channel channel,
                                                            io.grpc.CallOptions callOptions) {
            return new IstioCertificateServiceBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         * Using provided CSR, returns a signed certificate.
         * </pre>
         */
        public IstioCertificateResponse createCertificate(
            IstioCertificateRequest request) {
            return blockingUnaryCall(getChannel(), getCreateCertificateMethod(),
                getCallOptions(), request);
        }

    }

    /**
     * <pre>
     * Service for managing certificates issued by the CA.
     * </pre>
     */
    public static final class IstioCertificateServiceFutureStub
        extends io.grpc.stub.AbstractFutureStub<IstioCertificateServiceFutureStub> {

        private IstioCertificateServiceFutureStub(io.grpc.Channel channel,
                                                  io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @Override
        protected IstioCertificateServiceFutureStub build(io.grpc.Channel channel,
                                                          io.grpc.CallOptions callOptions) {
            return new IstioCertificateServiceFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         * Using provided CSR, returns a signed certificate.
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<IstioCertificateResponse> createCertificate(
            IstioCertificateRequest request) {
            return futureUnaryCall(
                getChannel().newCall(getCreateCertificateMethod(), getCallOptions()),
                request);
        }

    }

    private static final class MethodHandlers<Req, Resp>
        implements io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {

        private final IstioCertificateServiceImplBase serviceImpl;

        private final int methodId;

        MethodHandlers(IstioCertificateServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void invoke(Req request,
                           io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_CREATE_CERTIFICATE:
                    serviceImpl.createCertificate((IstioCertificateRequest) request,
                        (io.grpc.stub.StreamObserver<IstioCertificateResponse>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
            io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }

    }

    private static abstract class IstioCertificateServiceBaseDescriptorSupplier
        implements io.grpc.protobuf.ProtoFileDescriptorSupplier,
        io.grpc.protobuf.ProtoServiceDescriptorSupplier {

        IstioCertificateServiceBaseDescriptorSupplier() {
        }

        @Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return Ca.getDescriptor();
        }

        @Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("IstioCertificateService");
        }

    }

    private static final class IstioCertificateServiceFileDescriptorSupplier
        extends IstioCertificateServiceBaseDescriptorSupplier {

        IstioCertificateServiceFileDescriptorSupplier() {
        }

    }

    private static final class IstioCertificateServiceMethodDescriptorSupplier
        extends IstioCertificateServiceBaseDescriptorSupplier
        implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {

        private final String methodName;

        IstioCertificateServiceMethodDescriptorSupplier(String methodName) {
            this.methodName = methodName;
        }

        @Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }

    }

}
