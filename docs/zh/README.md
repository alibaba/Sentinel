## 概述

基于阿里巴巴的[Sentinel 面向分布式服务架构的高可用流量控制组件](https://sentinelguard.io/)和携程的[Apollo分布式配置中心](https://ctripcorp.github.io/apollo)定制

目的是为了在私有环境部署一套生产环境可用的sentinel-dashboard，用来对应用进行流量控制

仅限于应用数量小于500的场景，如果应用很多，请使用阿里云上提供企业级的 Sentinel 服务：[AHAS Sentinel 控制台](https://github.com/alibaba/Sentinel/wiki/AHAS-Sentinel-%E6%8E%A7%E5%88%B6%E5%8F%B0)

查看[快速开始](zh/deployment/quick-start)开始使用

查看[生产部署指南](zh/deployment/production-deployment-guide)将dashboard部署到生产环境

## 特性

* 运维简单
* TODO，待补充

## 示例

* https://github.com/Anilople/sentinel-demo-apollo

## 社区

在[discussions](https://github.com/Anilople/Sentinel/discussions)获取帮助

## 不足

* 实时监控不支持持久化
* 集群限流未支持