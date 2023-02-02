# Sentinel OpenSergo data-source

Sentinel OpenSergo data-source provides integration with OpenSergo.
The data source leverages [OpenSergo Java SDK](https://github.com/opensergo/opensergo-java-sdk) to implement subscribe (push) model.

## Usage

To use Sentinel OpenSergo data-source, you'll need to add the following Maven dependency:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-opensergo</artifactId>
    <version>x.y.z</version>
</dependency>
```

Then you can create an `OpenSergoDataSourceGroup` and subscribe Sentinel rules. For example:

```java
OpenSergoDataSourceGroup openSergo = new OpenSergoDataSourceGroup(host, port, namespace, appName);
openSergo.start();

// Subscribe flow rules from OpenSergo control plane, and propagate to Sentinel rule manager.
FlowRuleManager.register2Property(openSergo.subscribeFlowRules());
```