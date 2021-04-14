## 概述

如果你在

* 寻找分布式服务架构下的流量控制解决方案
* 寻找[Hystrix](https://github.com/Netflix/Hystrix)的替代方案

可以尝试使用这个项目，这是一个开源项目，在生态上如下图

![sentinel-opensource-eco-landscape-en.png](https://raw.githubusercontent.com/Anilople/Sentinel/master/doc/image/sentinel-opensource-eco-landscape-en.png)



本项目完成的事情是，结合阿里巴巴的[Sentinel 面向分布式服务架构的高可用流量控制组件](https://sentinelguard.io/)和携程的[Apollo分布式配置中心](https://ctripcorp.github.io/apollo)

给出了一个企业级的，分布式服务架构下的流量控制解决方案，并且可以在私有环境部署生产可用的[Sentinel控制台](https://github.com/alibaba/Sentinel/wiki/%E6%8E%A7%E5%88%B6%E5%8F%B0)，很适合无法直接在互联网上使用[AHAS Sentinel 控制台](https://github.com/alibaba/Sentinel/wiki/AHAS-Sentinel-%E6%8E%A7%E5%88%B6%E5%8F%B0)的公司

查看[快速开始](zh/deployment/quick-start)开始体验

查看[部署指南](zh/deployment/deployment-guide)将dashboard部署到生产环境

查看[设计/总览](zh/design/overview)了解项目设计方案

## 特性

* 流控，降级，熔断，热点流量限制，黑白名单规则
* 部署简单
* 运维简单
* 和Apollo配置中心无缝衔接
* 生产可用
* 可私有部署

## 示例

* [sentinel-demo-apollo](https://github.com/Anilople/sentinel-demo-apollo)

## 社区

在[discussions](https://github.com/Anilople/Sentinel/discussions)获取帮助
## 不足

* 实时监控不支持持久化
* 集群限流未支持
* 如果应用数量很多（超过500个），请分多套Sentinel控制台部署，或者使用阿里云上提供企业级的 Sentinel 服务：[AHAS Sentinel 控制台](https://github.com/alibaba/Sentinel/wiki/AHAS-Sentinel-%E6%8E%A7%E5%88%B6%E5%8F%B0)