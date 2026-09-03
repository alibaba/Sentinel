# Sentinel Spring AI MCP Adapter

This module adds tool-level Sentinel protection to MCP tools exposed by Spring AI.
It decorates MCP tool specifications instead of intercepting an HTTP transport, so the
same resource works with stdio, SSE, Streamable HTTP, WebMVC, and WebFlux servers.

The default resource name is `mcp:tool:<tool-name>`. Stateful and stateless tool
specifications are supported in both synchronous and asynchronous forms. Asynchronous
entries remain active until completion, error, or cancellation.

```java
SyncToolSpecification protectedTool =
    SentinelMcpToolSpecificationDecorator.decorate(toolSpecification);
```

```java
AsyncToolSpecification protectedTool =
    SentinelMcpToolSpecificationDecorator.decorate(toolSpecification);
```

The build baseline is Spring AI 2.0.1 and MCP Java SDK 2.0.0. The module requires
Java 17.
