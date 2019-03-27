# Sentinel Apache Dubbo Demo

This demo shows how to integrate Apache Dubbo **2.7.x or above version** with Sentinel
using `sentinel-apache-dubbo-adapter` module.

## Run the demo

For the provider demo `FooProviderBootstrap`, you need to add the following parameters when startup:

```shell
-Djava.net.preferIPv4Stack=true -Dproject.name=dubbo-provider-demo
```

For the consumer demo `FooConsumerBootstrap`, you need to add the following parameters when startup:

```shell
-Djava.net.preferIPv4Stack=true -Dproject.name=dubbo-consumer-demo
```
