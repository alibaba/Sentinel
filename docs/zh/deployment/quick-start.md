# 快速开始

这里提供一个快速体验的文档，如果查看项目简介，请参考[首页](zh/)

## 公网体验地址

### 服务端sentinel-dashboard

为了方便快速给个直观的感受，
已经在公网部署了控制台，直接访问
http://47.102.116.251:8080/
即可

![login](https://user-images.githubusercontent.com/15523186/112150933-9cf15780-8c1b-11eb-8e01-ef955089a5f9.png)

用户名和密码都是

```
sentinel
```

展开应用，然后点击**簇点链路**，即可直观得查看各个URL资源被访问的统计数据

![簇点链路](https://user-images.githubusercontent.com/15523186/112151118-d4600400-8c1b-11eb-8d2e-a007832a37c5.png)

### 客户端sentinel-demo-apollo

demo也已经部署到公网，源码地址是 https://github.com/Anilople/sentinel-demo-apollo.git

浏览器访问
http://47.102.116.251:8081/echo?id=3
后

可以在控制台看到

![sentinel-demo-apollo](https://user-images.githubusercontent.com/15523186/112155307-2145d980-8c20-11eb-8a3b-3e5252c200e6.png)

接下来可以对`sentinel-demo-apollo`编辑流控规则、降级规则、热点规则、系统规则、授权规则

相关的信息会持久化到Apollo配置中心里面

查看对`sentinel-demo-apollo`的配置是否生效，可以通过

* WebUI: dashboard
* actuator监控: http://47.102.116.251:8081/actuator/sentinel

例如，可以对资源`/echo`进行**新增流控规则**，限制QPS为1

![sentinel-demo-apollo 新增流控规则](https://user-images.githubusercontent.com/15523186/112155681-7e418f80-8c20-11eb-9343-cfb9f68ec782.png)

可以从控制台看到规则生效

![sentinel-demo-apollo 查看流控规则](https://user-images.githubusercontent.com/15523186/112156043-da0c1880-8c20-11eb-8675-066e77205e83.png)

再次用浏览器访问 http://47.102.116.251:8081/echo?id=3

如果刷新频率过高，会出现

```
Blocked by Sentinel (flow limiting)
```

或者

```
Whitelabel Error Page
This application has no explicit mapping for /error, so you are seeing this as a fallback.

Sat Apr 3 22:55:30 CST 2021
There was an unexpected error (type=Internal Server Error, status=500).
```

## 本地搭建

### 服务端sentinel-dashboard

在自己的机器上快速搭建sentinel控制台

从[releases](https://github.com/Anilople/Sentinel/releases)下载最新的.zip文件

确保自己的机器上，8080端口没有被占用

解压后，运行（Windows可以用Git Bash）

```bash
sh scripts/startup.sh
```

然后访问 http://localhost:8080

### 客户端sentinel-demo-apollo

确保自己的机器上，8081端口没有被占用

执行如下命令

```bash
git clone https://github.com/Anilople/sentinel-demo-apollo
cd sentinel-demo-apollo
mvn clean package -DskipTests
cd target
java -jar sentinel-demo-apollo*.jar
```

然后访问 http://localhost:8081/echo?id=3

再访问 http://localhost:8080

即可看到sentinel-demo-apollo的簇点链路

也可以下载源码后，自行用IDE启动sentinel-demo-apollo

## 后续

快速开始已经走完，

如果想了这个控制台是如何进行改造，接入Apollo配置中心的，可以参考[设计/总览](zh/design/overview)

如果想自己部署一套sentinel控制台，可以参考[部署/部署指南](zh/deployment/deployment-guide)

如果想将应用接入sentinel控制台，可以参考[使用/sentinel-客户端](zh/usage/sentinel-client)

## 参考资料

* 官方文档[Sentinel 控制台](https://sentinelguard.io/zh-cn/docs/dashboard.html)