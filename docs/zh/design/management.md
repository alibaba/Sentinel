
# 管理

## apollo

需要使用[Apollo开放平台](https://ctripcorp.github.io/apollo/#/zh/usage/apollo-open-api-platform)，这也是sentinel-dashboard可以修改sentinel客户端配置的关键

sentinel-dashboard需要作为一个第三方应用，获取一个token，然后用这个token，通过apollo open api去操作sentinel-dashboard自己，以及所有sentinel客户端的配置


## sentinel-dashboard

控制台自己的配置放在Apollo上

私有namespace：

* `application`：原封不动地放入官方的`application.properties`
* `custom`：放入自定义的配置，可以在这里覆盖官方的配置。Apollo Open Api的配置，logback的配置。
* `storage`：dashboard的key-value存储，运行时动态创建，目的是减少外部依赖（否则需要引入其它存储，例如MySQL）

公共namespace：

* `部门.sentinel`：给所有sentinel客户端使用，放置dashboard的地址，[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)的配置

## sentinel客户端

使用[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

为了和dashboard的无缝衔接，在设计上，只需要在`apollo.bootstrap.namespaces`中额外添加一个公共namespace，`部门.sentinel`，并授权给sentinel控制台使用的token，授权类型为App

公共namespace：

* `部门.sentinel`：获取dashboard相关信息，以及[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)的配置

私有namespace：

* `sentinel`：由sentinel-dashboard创建，并修改配置，sentinel-dashboard会将规则推送到这个私有namespace