# Sentinel Spring RestClient Adapter

## Overview

Sentinel Spring RestClient Adapter provides Sentinel integration for Spring Framework 6.0+ `RestClient`. With this adapter, you can easily add flow control, circuit breaking, and degradation features to HTTP requests made via `RestClient`.

## Features

- Flow control (QPS limiting)
- Circuit breaking (degradation)
- Custom resource name extraction
- Custom fallback responses
- HTTP 5xx error tracing

## Requirements

- Spring Framework 6.0+
- JDK 17+
- Sentinel Core 1.8.0+

## Usage

### 1. Add Dependency

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-spring-restclient-adapter</artifactId>
    <version>${sentinel.version}</version>
</dependency>
```

### 2. Basic Usage

```java
import com.alibaba.csp.sentinel.adapter.spring.restclient.SentinelRestClientInterceptor;
import org.springframework.web.client.RestClient;

// Create RestClient with Sentinel interceptor
RestClient restClient = RestClient.builder()
    .requestInterceptor(new SentinelRestClientInterceptor())
    .build();

// Use RestClient to send requests (protected by Sentinel)
String result = restClient.get()
    .uri("https://httpbin.org/get")
    .retrieve()
    .body(String.class);
```

### 3. Custom Configuration

```java
import com.alibaba.csp.sentinel.adapter.spring.restclient.SentinelRestClientConfig;
import com.alibaba.csp.sentinel.adapter.spring.restclient.SentinelRestClientInterceptor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.RestClientFallback;

// Custom resource name extractor
RestClientResourceExtractor customExtractor = request -> {
    // Example: normalize RESTful path parameters
    String path = request.getURI().getPath();
    if (path.matches("/users/\\d+")) {
        path = "/users/{id}";
    }
    return request.getMethod() + ":" + request.getURI().getHost() + path;
};

// Custom fallback: throw a custom exception when blocked
RestClientFallback customFallback = (request, body, execution, ex) -> {
    throw new RuntimeException("Service temporarily unavailable, please retry later", ex);
};

// Create configuration
SentinelRestClientConfig config = new SentinelRestClientConfig(
    "my-restclient:",  // Resource name prefix
    customExtractor,
    customFallback
);

// Create interceptor with custom configuration
RestClient restClient = RestClient.builder()
    .requestInterceptor(new SentinelRestClientInterceptor(config))
    .build();
```

### 4. Configure Sentinel Rules

```java
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import java.util.Collections;

// Configure flow control rule
FlowRule rule = new FlowRule("restclient:GET:https://httpbin.org/get");
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
rule.setCount(10); // Max 10 requests per second
rule.setLimitApp("default");

FlowRuleManager.loadRules(Collections.singletonList(rule));
```

## Core Components

### SentinelRestClientInterceptor

The main interceptor implementation responsible for:

- Creating Sentinel resources for each HTTP request
- Catching BlockException and invoking fallback handler
- Tracing exceptions and 5xx errors

### SentinelRestClientConfig

Configuration class containing:

- `resourcePrefix`: Resource name prefix (default: `restclient:`)
- `resourceExtractor`: Resource name extractor
- `fallback`: Fallback handler

### RestClientResourceExtractor

Interface for resource name extraction, allowing customization of resource name generation logic.

### RestClientFallback

Interface for fallback handling, invoked when requests are blocked by flow control or circuit breaking.

## Resource Name Format

The default resource name format: `{prefix}{METHOD}:{URL}`

Examples:

- `restclient:GET:https://httpbin.org/get`
- `restclient:POST:http://localhost:8080/api/users`

## Notes

This adapter only supports `RestClient` from Spring Framework 6.0+, not `RestTemplate`.

## Integration with Spring Cloud Alibaba

This adapter provides basic Sentinel integration. For Spring Cloud Alibaba projects:

1. Add auto-configuration support in `spring-cloud-starter-alibaba-sentinel`
2. Use `@SentinelRestClient` annotation for simplified configuration

## License

Apache License 2.0
