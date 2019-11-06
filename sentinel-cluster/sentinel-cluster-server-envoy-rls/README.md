# Sentinel Token Server (Envoy RLS implementation)

This module provides the [Envoy rate limiting gRPC service](https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_features/global_rate_limiting#arch-overview-rate-limit) implementation
with Sentinel token server.

> Note: the gRPC stub classes for Envoy RLS service is generated via `protobuf-maven-plugin` during the `compile` goal.
> The generated classes is located in the directory: `target/generated-sources/protobuf`.

## Build

Build the executable jar:

```bash
mvn clean package -P prod
```
