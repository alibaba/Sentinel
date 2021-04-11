# sentinel控制台

这里告诉读者，主要源码的修改点在哪些地方

这样读者就可以自行进行开发

修改源码的原则有：

* 尽量不修改官方源码，方便后续的升级

如果你想给这个项目添砖加瓦，欢迎提交Pull Request到https://github.com/Anilople/Sentinel

## 环境

* Git
* Maven
* Java 8及以上

## 构建

Shell执行

```bash
git clone https://github.com/Anilople/Sentinel.git
cd Sentinel
cd sentinel-dashboard
mvn clean package -DskipTests
```

> Windows环境下可以使用Git Bash

如果要自行构建在生产上可运行的版本，推荐在最新tag上构建，版本命名的规律参考[版本](#版本)

在target目录下，会产生一个`.zip`文件，可以用来部署Sentinel控制台，部署请参考[部署指南](zh/deployment/deployment-guide)

## 启动

使用IDE导入项目

```bash
git clone https://github.com/Anilople/Sentinel.git
cd Sentinel
cd sentinel-dashboard
```

只需要导入模块`sentinel-dashboard`即可

启动类为

```java
com.alibaba.csp.sentinel.dashboard.apollo.CustomDashboardApplication
```

启动后，在本地访问

http://localhost:8080/

后面即可进行调试

使用的Apollo配置中心地址请参考[配置](#配置)

## 前端代码

主要修改了sentinel控制台**首页**的代码

HTML文件源码在[sentinel-dashboard/src/main/webapp/resources/app/views/dashboard/home.html](https://github.com/Anilople/Sentinel/blob/master/sentinel-dashboard/src/main/webapp/resources/app/views/dashboard/home.html)

## 后端代码

Java代码都在package [com.alibaba.csp.sentinel.dashboard.apollo](https://github.com/Anilople/Sentinel/tree/master/sentinel-dashboard/src/main/java/com/alibaba/csp/sentinel/dashboard/apollo) 中

## 配置

使用的配置中心是https://github.com/ctripcorp/apollo提供的公网环境

演示环境（Demo）

- [106.54.227.205](http://106.54.227.205/)
- 账号/密码:apollo/admin

```properties
apollo.meta = http://106.54.227.205:8080
```

相关配置在文件`src/main/resources/application.properties`中

## 日志

删除了本地的logback文件

设计上请参考[设计/Sentinel控制台#日志](zh/design/sentinel-dashboard#日志)

## 版本

由于只修改了Sentinel控制台，所以所有源码都在maven模块`sentinel-dashboard`中

基于[alibaba/Sentinel](https://github.com/alibaba/Sentinel)的最新版本开发

如果官方的最新版本为`x.y.z`，那么定制版的版本命名规律为`x.y.z-n-suffix`

| 单词     | 含义                                                         |
| -------- | ------------------------------------------------------------ |
| `x.y.z`  | [alibaba/Sentinel](https://github.com/alibaba/Sentinel)Release的版本号 |
| ` -`     | 分隔符                                                       |
| `n`      | 数字，从0开始，每当定制版想升级时，就递增                    |
| `suffix` | SNAPSHOT或者RELEASE                                          |

例如，Sentinel官方最新版本为`1.8.1`

那么Sentinel控制台定制版的版本号迭代如下

```
1.8.1-0-SNAPSHOT
1.8.1-0-RELEASE
1.8.1-1-SNAPSHOT
1.8.1-1-RELEASE
1.8.1-2-SNAPSHOT
1.8.1-2-RELEASE
...
```

每次RELEASE都会对应一个和版本同名的tag

> 由于历史原因，有些

这个信息存储在模块`sentinel-dashboard`，文件`pom.xml`的`version`中

参考某个commit下的[sentinel-dashboard/pom.xml#L14](https://github.com/Anilople/Sentinel/blob/2d3c05e31b6d75d6259a18305e7b25b4650b2f92/sentinel-dashboard/pom.xml#L14)



