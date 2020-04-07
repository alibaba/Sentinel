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

## Rule configuration

Sentinel RLS token server supports dynamic rule configuration via the yaml file.
The file may provide rules for one *domain* (defined in Envoy's conf file).
In Envoy, one rate limit request might carry multiple *rate limit descriptors*
(which will be generated from [Envoy rate limit actions](https://www.envoyproxy.io/docs/envoy/v1.12.1/api-v2/api/v2/route/route.proto#envoy-api-msg-route-ratelimit)).
One rate limit descriptor may have multiple entries (key-value pair).
We may set different threshold for each rate limit descriptors.

A sample rule configuration file:

```yaml
domain: foo
descriptors:
  - resources:
    - key: "destination_cluster"
      value: "service_httpbin"
    count: 1
```

This rule only takes effect for domain `foo`. It will limit the max QPS to 1 for
all requests targeted to the `service_httpbin` cluster.

We need to provide the path to yaml file via the `SENTINEL_RLS_RULE_FILE_PATH` env
(or `-Dcsp.sentinel.rls.rule.file` opts). Then as soon as the content in the rule file has been changed,
Sentinel will reload the new rules from the file to the `EnvoyRlsRuleManager`.

We may check the logs in `~/logs/csp/sentinel-record.log.xxx` to see whether the rules has been loaded.
We may also retrieve the converted `FlowRule` via the command API `localhost:8719/cluster/server/flowRules`.

## Configuration items

The configuration list:

| Item (env) | Item (JVM property) | Description | Default Value | Required |
|--------|--------|--------|--------|--------|
| `SENTINEL_RLS_GRPC_PORT` | `csp.sentinel.grpc.server.port` | The RLS gRPC server port | **10240** | false |
| `SENTINEL_RLS_RULE_FILE_PATH` | `csp.sentinel.rls.rule.file` | The path of the RLS rule yaml file | - | **true** |
| `SENTINEL_RLS_ACCESS_LOG` | - | Whether to enable the access log (`on` for enable) | off | false |

## Samples

- [Kubernetes sample](./sample/k8s)
