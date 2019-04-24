# Sentinel API Gateway Adapter Common

The `sentinel-api-gateway-adapter-common` module provides common abstraction for
API gateway flow control:

- `GatewayFlowRule`: flow control rule specific for route or API defined in API gateway.
This can be automatically converted to `FlowRule` or `ParamFlowRule`.
- `ApiDefinition`: gateway API definition with a group of predicates