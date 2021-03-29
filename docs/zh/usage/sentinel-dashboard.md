# sentinel-dashboard

sentinel控制台在使用上，基本都是WebUI上的操作

主要的功能都在**首页**（左上方）上

![左上方-首页](https://user-images.githubusercontent.com/15523186/112841525-2947c280-90d3-11eb-9a96-480108c01da5.png)

## 规则迁移

可以将一个sentinel控制台上看到的规则，进行导出，然后再在另一个sentinel控制台，进行导入，这样运维上非常方便

### 规则导出

可以将一个应用的规则，或者多个应用的规则，导出成.zip文件

![config-export](https://user-images.githubusercontent.com/15523186/112841415-06b5a980-90d3-11eb-9d36-6efc52896ac7.png)

点击导出即可

会生成一个.zip文件

注意：如果不是特殊需求，推荐每次只导出一个应用的规则

注意：在**首页**有个按钮可以导出所有应用的所有规则

### 规则导入

使用.zip文件，在**首页**可以导入规则

![规则导入](https://user-images.githubusercontent.com/15523186/112841684-5bf1bb00-90d3-11eb-91a7-a351f2de996f.png)

选择.zip文件，然后再点击导入即可

注意：会覆盖已有的规则

## 应用基本操作

参考sentinel控制台首页中的**应用基本操作**

### 源码

代码在`com.alibaba.csp.sentinel.dashboard.apollo.controller.SentinelApolloController`中