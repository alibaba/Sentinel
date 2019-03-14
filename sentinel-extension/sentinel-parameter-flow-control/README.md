# Sentinel Parameter Flow Control

This component provides functionality of flow control by frequent ("hot spot") parameters.

## Usage

To use Sentinel Parameter Flow Control, you need to add the following dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-parameter-flow-control</artifactId>
    <version>x.y.z</version>
</dependency>
```

First you need to pass parameters with the following `SphU.entry` overloaded methods:

```java
public static Entry entry(String name, EntryType type, int count, Object... args) throws BlockException

public static Entry entry(Method method, EntryType type, int count, Object... args) throws BlockException
```

For example, if there are two parameters to provide, you can:

```java
// paramA in index 0, paramB in index 1.
SphU.entry(resourceName, EntryType.IN, 1, paramA, paramB);
```

Then you can configure parameter flow control rules via `loadRules` method in `ParamFlowRuleManager`:

```java
// QPS mode, threshold is 5 for every frequent "hot spot" parameter in index 0 (the first arg).
ParamFlowRule rule = new ParamFlowRule(RESOURCE_KEY)
    .setParamIdx(0)
    .setCount(5);
// We can set threshold count for specific parameter value individually.
// Here we add an exception item. That means: QPS threshold of entries with parameter `PARAM_B` (type: int)
// in index 0 will be 10, rather than the global threshold (5).
ParamFlowItem item = new ParamFlowItem().setObject(String.valueOf(PARAM_B))
    .setClassType(int.class.getName())
    .setCount(10);
rule.setParamFlowItemList(Collections.singletonList(item));
ParamFlowRuleManager.loadRules(Collections.singletonList(rule));
```

The description for fields of `ParamFlowRule`:

| Field | Description | Default |
| :----: | :----| :----|
| resource| resource name (**required**) ||
| count | flow control threshold (**required**) ||
| grade | metric type (QPS or thread count) | QPS mode |
| paramIdx | the index of provided parameter in `SphU.entry(xxx, args)` (**required**) ||
| paramFlowItemList | the exception items of parameter; you can set threshold to a specific parameter value ||

Now the parameter flow control rules will take effect.