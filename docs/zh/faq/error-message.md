# 错误信息

## sentinel-dashboard

sentinel控制台错误信息排查

### 推送失败：unsupported operation

说明这个操作从sentinel控制台的源码层面，还未支持

无法解决

### Create app namespace: xxx failed

例如

```
Create app namespace: sentinel for appId: sentinel-demo-apollo, format: properties failed
```

描述的是给项目`sentinel-demo-apollo`创建一个格式为`properties`，namespace名字为`sentinel`的namespace失败

通常是没有对`sentinel-demo-apollo`进行赋权，导致sentinel控制台无法修改配置，需要用管理员账户登录到Apollo portal，进行赋权

### 失败：Create cluster: xxx failed

例如

```
失败：Create cluster: default for appId: sentinel-demo-apollo in env: DEV failed
```

尝试给项目`sentinel-demo-apollo`在DEV环境创建集群`default`，但是创建失败

如果集群的名字是`default`，一般是项目不存在，可以复制项目信息`sentinel-demo-apollo`到Apollo portal中进行查询

如果集群的名字不是`default`，是没有对`sentinel-demo-apollo`进行赋权，导致sentinel控制台无法修改配置

## sentinel-client

sentinel客户端错误信息排查

### RemoteConfigRepository Load config failed

例如

```java
2021-03-27 11:59:08.479  WARN 9271 --- [figRepository-1] c.c.f.a.i.RemoteConfigRepository         : Load config failed, will retry in 1 SECONDS. appId: sentinel-demo-apollo, cluster: default, namespaces: application
2021-03-27 11:59:09.486  WARN 9271 --- [figRepository-1] c.c.f.a.i.AbstractConfigRepository       : Sync config failed, will retry. Repository class com.ctrip.framework.apollo.internals.RemoteConfigRepository, reason: Load Apollo Config failed - appId: sentinel-demo-apollo, cluster: default, namespace: application, url: http://192.168.233.5:8080/configs/sentinel-demo-apollo/default/application?ip=192.168.233.10&messages=%7B%22details%22%3A%7B%22sentinel-demo-apollo%2Bdefault%2Bapplication%22%3A35%7D%7D&releaseKey=20210327235210-675eb3eed6245a7d [Cause: [status code: 404] Could not find config for namespace - appId: sentinel-demo-apollo, cluster: default, namespace: application, please check whether the configs are released in Apollo!]
```

代表项目sentinel-demo-apollo尝试获取，集群default里，名字为application的namespace，但是获取失败了，这种情况属于namespace未发布，需要在Apollo Portal上点击一下发布即可

如果namespace的名字sentinel，例如

```java
2021-03-27 11:44:13.442  WARN 9271 --- [           main] c.c.f.a.i.RemoteConfigRepository         : Load config failed, will retry in 1 SECONDS. appId: sentinel-demo-apollo, cluster: default, namespaces: sentinel
2021-03-27 11:44:14.448  WARN 9271 --- [           main] c.c.f.a.i.AbstractConfigRepository       : Sync config failed, will retry. Repository class com.ctrip.framework.apollo.internals.RemoteConfigRepository, reason: Load Apollo Config failed - appId: sentinel-demo-apollo, cluster: default, namespace: sentinel, url: http://192.168.233.5:8080/configs/sentinel-demo-apollo/default/sentinel?ip=192.168.233.10 [Cause: [status code: 404] Could not find config for namespace - appId: sentinel-demo-apollo, cluster: default, namespace: sentinel, please check whether the configs are released in Apollo!]
```

名字为sentinel的namespace，是用来存储流控规则的，说明sentinel控制台没有为sentinel-demo-apollo发布这个namespace

此时需要在sentinel控制台上为应用添加流控规则

### AbstractConfigRepository Sync config failed

同上