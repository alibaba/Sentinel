# Sentinel gRPC Adapter

Sentinel gRPC Adapter provides client and server interceptor for gRPC services.

> Note that currently the interceptor only supports unary methods in gRPC.

## Client Interceptor

Example:

```java
public class ServiceClient {

    private final ManagedChannel channel;

    ServiceClient(String host, int port) {
        this.channel = ManagedChannelBuilder.forAddress(host, port)
            .intercept(new SentinelGrpcClientInterceptor()) // Add the client interceptor.
            .build();
        // Init your stub here.
    }
}
```

## Server Interceptor

Example:

```java
import io.grpc.Server;

Server server = ServerBuilder.forPort(port)
     .addService(new MyServiceImpl()) // Add your service.
     .intercept(new SentinelGrpcServerInterceptor()) // Add the server interceptor.
     .build();
```
