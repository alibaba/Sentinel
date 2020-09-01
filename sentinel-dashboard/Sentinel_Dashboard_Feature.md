# Sentinel 控制台功能介绍

## 0. 概述

Sentinel 控制台是流量控制、熔断降级规则统一配置和管理的入口，它为用户提供了机器自发现、簇点链路自发现、监控、规则配置等功能。在 Sentinel 控制台上，我们可以配置规则并实时查看流量控制效果。使用 Sentinel 控制台的流程如下：

```
客户端接入 -> 机器自发现 -> 查看簇点链路 -> 配置流控规则 -> 查看流控效果
```

## 1. 功能介绍

### 1.1 机器自发现

Sentinel 提供内置的机器自发现功能，无需依赖第三方服务发现组件即可实现客户端的发现。点击 Sentinel 控制台左侧导航栏的“机器列表”菜单，可查看集群机器数量和机器健康状况。

### 1.2 簇点链路自发现

Sentinel 将每一个需要流控的 URL 或者接口称为一个资源，并使用URL地址或方法签名表示资源。Sentinel 会自动发现所有潜在的资源和资源之间的调用链，并在“簇点链路”页面展示。
资源是设置流控规则的载体，通过簇点链路自发现，可以方便的对重要资源设置流控规则、熔断降级规则等。

> 注意：**客户端有访问量之后，才能在簇点链路页面看到资源监控（lazy-initializing）**。

### 1.3 实时监控

Sentinel 监控功能能够实时查看集群中每个资源的实时访问以及流控情况。控制台左侧导航栏的“实时监控”菜单对应该功能。

### 1.4 流控降级规则设置

Sentinel 提供了多种规则来保护系统的不同部分。流量控制规则用于保护服务提供方，熔断降级规则用于保护服务消费方，系统保护规则用于保护整个系统。

#### 1.4.1 流量控制规则

流量控制规则用于保护服务提供方，服务提供方指任何可以对外提供服务的系统，比如向终端用户提供 Web 服务的 Web 应用，向微服务消费方提供服务的 Dubbo Service Provider 等。
系统的服务能力是有限的，如果消费方请求速度过高，则采用相应的保护策略，或是直接拒绝，或是排队等待。通过“流控规则”页面可以查看和配置流量控制规则。

#### 1.4.2 熔断降级规则

降级规则用于保护服务消费方。在微服务架构中，业务系统通常要依赖多个服务，依赖的某个服务不可用将会影响业务系统的可用性。解决这个问题的方法是及时发现服务的“不可用”状态，在调用时快速失败而不是等待调动超时或者重试。Sentinel 通过熔断降级来达到快速失败的目的。通过“降级规则”页面可以查看和配置降级规则。

#### 1.4.3 系统保护规则

系统保护规则（简称`系统规则`）用于保护整个系统指标处于安全水位。无论是服务提供方还是消费方，活跃线程数过多、系统LOAD等过高都是系统处于高水位的指标。当系统水位过高时，系统应拒绝对外提供服务以便迅速降低资源占用。通过“系统规则”页面可以查看和设置系统保护规则。

## 2. 限制

本控制台只是用于演示 Sentinel 的基本能力和工作流程，并没有依赖生产环境中所必需的组件，比如**持久化的后端数据库、可靠的配置中心**等。
目前 Sentinel 采用内存态的方式存储监控和规则数据，监控最长存储时间为 5 分钟，控制台重启后数据丢失。

## 3. 配置项

控制台的一些特性可以通过配置项来进行配置，配置项主要有两个来源：`System.getProperty()` 和 `System.getenv()`，同时存在时后者可以覆盖前者。

> 通过环境变量进行配置时，因为不支持 `.` 所以需要将其更换为 `_`。

项 | 类型 | 默认值 | 最小值 | 描述
--- | --- | --- | --- | ---
sentinel.dashboard.auth.username | String | sentinel | 无 | 登录控制台的用户名，默认为 `sentinel`
sentinel.dashboard.auth.password | String | sentinel | 无 | 登录控制台的密码，默认为 `sentinel`
sentinel.dashboard.app.hideAppNoMachineMillis | Integer | 0 | 60000 | 是否隐藏无健康节点的应用，距离最近一次主机心跳时间的毫秒数，默认关闭
sentinel.dashboard.removeAppNoMachineMillis | Integer | 0 | 120000 | 是否自动删除无健康节点的应用，距离最近一次其下节点的心跳时间毫秒数，默认关闭
sentinel.dashboard.unhealthyMachineMillis | Integer | 60000 | 30000 | 主机失联判定，不可关闭
sentinel.dashboard.autoRemoveMachineMillis | Integer | 0 | 300000 | 距离最近心跳时间超过指定时间是否自动删除失联节点，默认关闭
datasource.provider | String | memory | 无 | 默认为 `memory`, 可选持久化配置 `nacos`、`apollo`、`zookeeper`
datasource.provider.nacos.server-addr | String | localhost:8848 | 无 | nacos 注册中心地址
datasource.provider.nacos.username | String |  | 无 | nacos 用户名，默认为空
datasource.provider.nacos.password | String |  | 无 | nacos 密码，默认为空
datasource.provider.nacos.namespace | String |  | 无 | nacos 名臣空间，默认为空
datasource.provider.nacos.group-id | String | SENTINEL_GROUP | 无 | nacos 分组，默认为 `SENTINEL_GROUP`
datasource.provider.apollo.server-addr | String | http://localhost:10034 | 无 | apollo 注册中心地址，必须有前缀 `http://` 或 `https://`
datasource.provider.apollo.token | String | token | 无 | apollo 登录 token，默认为 `token`
datasource.provider.zookeeper.server-addr | String | localhost:2181 | 无 | zookeeper 注册中心地址
datasource.provider.zookeeper.session-timeout | Integer | 60000 | 0 | zookeeper session超时时间，默认 `60000`
datasource.provider.zookeeper.connection-timeout |  Integer | 15000 | 0 | zookeeper connection超时时间，默认 `15000`
datasource.provider.zookeeper.retry.max-retries | Integer | 3 | 0 | zookeeper 最大重试次数， 默认 `3`
datasource.provider.zookeeper.retry.base-sleep-time | Integer | 1000 | 1000 | zookeeper 重试间隔最小时长，默认 `1000`
datasource.provider.zookeeper.retry.max-sleep-time | Integer | 2147483647 | 0 | zookeeper 重试间隔最大时长，默认 `2147483647`

配置示例：

- 命令行方式：

```shell
java -Dsentinel.dashboard.app.hideAppNoMachineMillis=60000
```

- Java 方式：

```java
System.setProperty("sentinel.dashboard.app.hideAppNoMachineMillis", "60000");
```

- 环境变量方式：

```shell
sentinel_dashboard_app_hideAppNoMachineMillis=60000
```

更多：

- [Sentinel 控制台启动和客户端接入](./README.md)
- [控制台 Wiki](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0)