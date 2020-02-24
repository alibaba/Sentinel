# Awesome Sentinel

[![Awesome](https://awesome.re/badge-flat.svg)](https://awesome.re)

A curated list of awesome things (e.g. samples, third-party extensions, blog posts) for [Sentinel](https://github.com/alibaba/Sentinel).

If you want your component to appear here, feel free to submit a pull request to this repository to add it.
You can refer to the [awesome contribution guidelines](https://github.com/sentinel-group/sentinel-awesome/blob/master/CONTRIBUTING.md).

You can also add to [sentinel-group/sentinel-awesome](https://github.com/sentinel-group/sentinel-awesome).

## Contents

- [Presentations](#presentations)
- [Tutorials](#tutorials)
- [Demos](#demos)
- [Extensions / Integrations](#extensions--integrations)
- [Blog Posts](#blog-posts)

## Presentations

- Sentinel 1.6.0 网关流控新特性介绍-Eric Zhao (Dubbo Tech Day-201905-Beijing): [PDF](https://github.com/sentinel-group/sentinel-awesome/blob/master/slides/Sentinel%201.6.0%20网关流控新特性介绍-Eric%20Zhao-DTED-201905.pdf)
- Sentinel 微服务流控降级实践-Eric Zhao (Dubbo Tech Day-201907-Shenzhen): [PDF](https://github.com/sentinel-group/sentinel-awesome/blob/master/slides/Sentinel%20微服务流控降级实践-Eric%20Zhao-DTED-201907.pdf)
- Sentinel 1.7.0 新特性展望-Eric Zhao (Dubbo Tech Day-201910-Chengdu): [PDF](https://github.com/sentinel-group/sentinel-awesome/blob/master/slides/Sentinel%201.7.0%20新特性展望-Eric%20Zhao-DTED-201910.pdf)

## Tutorials

- [Sentinel Guides](https://github.com/sentinel-group/sentinel-guides)

## Demos

- [sentinel-zuul-example](https://github.com/tigerMoon/sentinel-zuul-sample): A simple project integration Sentinel to Spring Cloud Zuul which provide Service and API Path level flow control management by [tiger](https://github.com/tigerMoon)

## Extensions / Integrations

- [sentinel-support](https://github.com/cdfive/sentinel-support): A support project for convenient Sentinel integration including properties file configuration, ActiveMQ integration and a JdbcDataSource implementation by [cdfive](https://github.com/cdfive)
- [Sentinel dashboard multi-data-source adapter](https://github.com/finefuture/sentinel-dashboard-X): Sentinel dashboard multi-data-source adapter has integrated Apollo and Nacos configuration center for bidirectional modification persistence. Implemented by [finefuture](https://github.com/finefuture)
- [Sentinel Rule Annotation Support](https://github.com/code1986/sentinel-lib): A third-party library that supports configuring flow rule and degrade rule using annotation. Implemented by [code1986](https://github.com/code1986)
- [sentinel-pigeon-adapter](https://github.com/wchswchs/sentinel-pigeon): A RPC framework Pigeon adapter for Sentinel including provider and invoker rate limiting implementation by [wchswchs](https://github.com/wchswchs)

## Blog Posts

- [Sentinel 为 Dubbo 服务保驾护航](http://dubbo.apache.org/zh-cn/blog/sentinel-introduction-for-dubbo.html) by [Eric Zhao](https://github.com/sczyh30)
- [在生产环境中使用 Sentinel 控制台](https://github.com/alibaba/Sentinel/wiki/在生产环境中使用-Sentinel) by [Eric Zhao](https://github.com/sczyh30)
- [Sentinel 与 Hystrix 的对比](https://sentinelguard.io/zh-cn/blog/sentinel-vs-hystrix.html) by [Eric Zhao](https://github.com/sczyh30)
- [Guideline: 从 Hystrix 迁移到 Sentinel](https://sentinelguard.io/zh-cn/blog/guideline-migrate-from-hystrix-to-sentinel.html) by [Eric Zhao](https://github.com/sczyh30)
- [Sentinel 控制台监控数据持久化【MySQL】](https://www.cnblogs.com/cdfive2018/p/9838577.html) by [cdfive](https://github.com/cdfive)
- [Sentinel 控制台监控数据持久化【InfluxDB】](https://www.cnblogs.com/cdfive2018/p/9914838.html) by [cdfive](https://github.com/cdfive)
- [Sentinel 控制台监控数据持久化【Apollo】](https://blog.csdn.net/caodegao/article/details/100009618) by [cookiejoo](https://github.com/cookiejoo)
- [Sentinel一体化监控解决方案 CrateDB + Grafana](https://blog.csdn.net/huyong1990/article/details/82392386) by [Young Hu](https://github.com/YoungHu)
- Sentinel 源码解析系列 by [houyi](https://github.com/all4you)
  - [Sentinel 原理-全解析](https://mp.weixin.qq.com/s/7_pCkamNv0269e5l9_Wz7w)
  - [Sentinel 原理-调用链](https://mp.weixin.qq.com/s/UEzwD22YC6jpp02foNSXnw)
  - [Sentinel 原理-滑动窗口](https://mp.weixin.qq.com/s/B1_7Kb_CxeKEAv43kdCWOA)
  - [Sentinel 实战-限流](https://mp.weixin.qq.com/s/rjyU37Dm-sxNln7GUD8tOw)
  - [Sentinel 实战-控制台](https://mp.weixin.qq.com/s/23EDFHMXLwsDqw-4O5dR5A)
  - [Sentinel 实战-规则持久化](https://mp.weixin.qq.com/s/twMFiBfRawKLR-1-N-f1yw)
- Sentinel 学习笔记 by [ro9er](https://github.com/ro9er)
  - [Sentinel 学习笔记（1）-- 流量统计代码解析](https://www.jianshu.com/p/7936d7a57924)
  - [Sentinel 学习笔记（2）-- 流量控制代码分析](https://www.jianshu.com/p/938709e94e43)
  - [Sentinel 学习笔记（3）-- 上下文统计Node建立分析](https://www.jianshu.com/p/cfdf525248c1)
- [大流量下的服务质量治理 Dubbo Sentinel 初涉](https://mp.weixin.qq.com/s/ergr_siI07VwwSRPFgsLvQ) by [RyuGrade](https://github.com/RyuGrade)
- Sentinel 深入浅出系列 by [shxz130](https://github.com/shxz130)
  - [Sentinel 深入浅出之原理篇 SlotChain](https://www.jianshu.com/p/a7a405de3a12)
  - [Sentinel 深入浅出之原理篇 Context初始化 & Entry初始化](https://www.jianshu.com/p/e39ac47cd893)
  - [Sentinel 深入浅出之原理篇 NodeSelectorSlot](https://www.jianshu.com/p/9a380ba188ab)
  - [Sentinel 深入浅出之原理篇 ClusterBuilderSlot](https://www.jianshu.com/p/0b0b5d8888a2)
  - [Sentinel 深入浅出之原理篇 StatisticSlot&滑动窗口](https://www.jianshu.com/p/9620298fd15a)
  - [Sentinel 深入浅出之原理篇 SystemSlot](https://www.jianshu.com/p/bfad1b7d0cde)
  - [Sentinel 深入浅出之原理篇 AuthoritySlot](https://www.jianshu.com/p/c5312c2242b3)
  - [Sentinel 深入浅出之原理篇 FlowSlot](https://www.jianshu.com/p/53218d0d273e)
  - [Sentinel 深入浅出之原理篇 DegradeSlot](https://www.jianshu.com/p/e910d4840e4a)
- [Alibaba Sentinel RESTful 接口流控处理优化](https://www.jianshu.com/p/96f5980d9798) by [luanlouis](https://github.com/luanlouis)
- [Sentinel 控制台前端开发环境搭建](https://www.cnblogs.com/cdfive2018/p/11084001.html) by [cdfive](https://github.com/cdfive)
- [阿里 Sentinel 源码解析](https://www.javadoop.com/post/sentinel) by [Javadoop](https://www.javadoop.com)
