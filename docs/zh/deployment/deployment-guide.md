# 部署指南

## 运行环境准备

### OS

服务端是一个Spring Boot，理论上支持所有Linux发行版

### Apollo

由于sentinel控制台基于Apollo配置中心定制，所以需要有一套现成的Apollo

这里以 https://github.com/ctripcorp/apollo

中提供的公网环境作为演示

```properties
apollo.meta = http://106.54.227.205:8080/
apollo.cluster = beijing
env = DEV
```

portal地址为http://106.54.227.205/

演示中使用的`app.id`定为`example.sentinel.dashboard`，部门是**平台部(PT)**

自行部署时需要修改上述的信息

### Java

至少需要Java 8运行环境来运行sentinel的控制台

### 安装介质

只有一个`.zip`文件

命名规则为sentinel-dashboard-xxx.zip，`xxx`为maven版本号

可以自行通过源码构建，执行如下命令，即可看到生成的.zip文件

```bash
git clone https://github.com/Anilople/Sentinel.git
cd Sentinel/sentinel-dashboard
mvn clean package -DskipTests
cd target
```

也可以通过https://github.com/Anilople/Sentinel/releases

直接下载最新版.zip文件

## Apollo开放平台授权管理-创建第三方应用

给Sentinel控制台创建第三方应用

Chrome浏览器打开http://106.54.227.205/

Username: apollo

Password: admin

进行登录

点击右上角的**管理员工具**

点击**开放平台授权管理**

![开放平台授权管理](https://user-images.githubusercontent.com/15523186/112304979-1ac76880-8cd9-11eb-97d4-3715aa8d40d3.png)

创建第三方应用，推荐第三方应用ID和sentinel控制台在Apollo项目的app.id保持一致

![创建第三方应用](https://user-images.githubusercontent.com/15523186/112305272-73970100-8cd9-11eb-8425-3996dba68585.png)

创建成功后会获得一个token，将token记录下来，后续使用

![token](https://user-images.githubusercontent.com/15523186/112305410-9fb28200-8cd9-11eb-9910-0a3a4f85b5db.png)

在这里演示的是Token值为`e73070a5914099a7d4f1ceeac31c5cd055231994`

## Apollo-Portal

给Sentinel控制台创建项目，管理它的配置

Chrome浏览器打开http://106.54.227.205/

Username: apollo

Password: admin

进行登录

### 创建项目

点击创建项目

按如下表单填入相应信息

* 部门：运维(OP)
* AppId：example.sentinel.dashboard
* 应用名称：example.sentinel.dashboard
* 应用负责人：apollo
* 项目管理员：apollo

![创建项目](https://user-images.githubusercontent.com/15523186/112301340-ca4e0c00-8cd4-11eb-8762-f41f51a55ae0.png)

然后点击提交

进入项目

### 添加namespace

#### custom

这是一个私有的namespace，详细创建过程如下述步骤

点击左下角的**添加Namespace**

![添加namespace](https://user-images.githubusercontent.com/15523186/112301460-ece02500-8cd4-11eb-8094-c014e6eea608.png)

点击右上角的**创建Namespace**

![创建Namespace](https://user-images.githubusercontent.com/15523186/112299296-a093e580-8cd2-11eb-80fc-1ef6f3e92830.png)

类型选择**private**，名称填入**custom**

![私有namespace](https://user-images.githubusercontent.com/15523186/112299932-42b3cd80-8cd3-11eb-814f-c6445f6cd114.png)

点击**提交**，创建出私有namespace

点击右上角的**返回到项目首页**

此时可以看到私有namespace已经创建出来

#### orgId.sentinel

orgId是部门的标识，实际情况应该根据部门来进行选择

点击左下角的**添加Namespace**

![custom](https://user-images.githubusercontent.com/15523186/112301597-113c0180-8cd5-11eb-8177-22bccb117764.png)

点击右上角的**创建Namespace**

![创建Namespace](https://user-images.githubusercontent.com/15523186/112299296-a093e580-8cd2-11eb-80fc-1ef6f3e92830.png)

类型选择**public**

名称填入**sentinel**

![orgId.sentinel](https://user-images.githubusercontent.com/15523186/112301736-37fa3800-8cd5-11eb-80e6-c42d2bbb25c8.png)

点击提交

点击右上角的**返回到项目首页**

此时可以看到公共namespace被创建出来

![公共namespace](https://user-images.githubusercontent.com/15523186/112301984-7e4f9700-8cd5-11eb-92d1-e0ebbf09d90f.png)

### 添加集群

点击左下角的**添加集群**

![添加集群](https://user-images.githubusercontent.com/15523186/112302120-ad660880-8cd5-11eb-9574-026fe9aac386.png)

勾选环境**DEV**

并填入集群名称**beijing**

![image-20210324191920688](C:\Users\anilo\AppData\Roaming\Typora\typora-user-images\image-20210324191920688.png)

点击**提交**

点击右上角的**返回到项目首页**

此时可以看到集群已经被创建出来，并且之前创建的namespace也被创建出来了

![image-20210324192017979](C:\Users\anilo\AppData\Roaming\Typora\typora-user-images\image-20210324192017979.png)

### 放入配置

集群和相关的namespace已经被创建出来了

接下来是把配置放入

所有的配置都在sentinel-dashboard-xxx.zip文件的namespaces目录下，你可以从中获取，部分内容需要修改

点击**beijing**集群，修改如下的namespace，并发布

#### application

这里放的是sentinel官方的配置，原封不动地放入，然后发布

#### custom

控制台的私有配置，有一些个性化的内容，覆盖sentinel官方的默认配置（例如用户名和密码），这里修改如下内容

```properties
# apollo portal地址
sentinel.apollo.open.api.portal-url = http://106.54.227.205/
# apollo开放平台的token
sentinel.apollo.open.api.token = e73070a5914099a7d4f1ceeac31c5cd055231994
# apollo开放平台的操作用户，必须存在，建议和sentinel控制台项目的用户保持一致
sentinel.apollo.open.api.operated-user = apollo
# apollo开放平台要操作的环境，DEV为默认值，如果是其它环境，请修改，例如是PRO环境可以将DEV改为PRO
sentinel.apollo.open.api.operated-env = ${env:${ENV:DEV}}
```

放入，然后发布

#### orgId.sentinel

这里使用的是`OP.sentinel`

需要修改

```properties
spring.cloud.sentinel.transport.dashboard = localhost:8080
```

这里演示使用的是本地，实际部署时，请修改成Linux服务的IP，端口也要和custom中的`server.port`对应

## sentinel控制台

控制台在Apollo上的配置已经准备好，接下来在Linux服务器上部署

### 传输sentinel-dashboard-xxx.zip文件到Linux服务器

创建目录

```bash
mkdir ~/sentinel
```

传输文件sentinel-dashboard-xxx.zip到目录`~/sentinel`下

解压并进入

```bash
cd ~/sentinel
unzip sentinel-dashboard-xxx.zip
```

### 修改本地配置

进入控制台.jar文件所处目录

```bash
cd ~/sentinel/sentinel-dashboard-xxx
```

修改sentinel控制台的本地配置文件`config/application.properties`

```properties
app.id = example.sentinel.dashboard
apollo.meta = http://106.54.227.205:8080
apollo.cluster = beijing
apollo.bootstrap.namespaces = custom,application,OP.sentinel
```

注意

apollo.cluster需要修改成beijing

apollo.bootstrap.namespaces最后一个namespace，也就是公共namespace，要修改成OP.sentinel

### 启动

```bash
sh scripts/startup.sh
```

控制台日志已经关闭，所以查看console.log文件，只能看到如下内容

```java
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.9.RELEASE)

INFO: Sentinel log output type is: file
INFO: Sentinel log charset is: utf-8
INFO: Sentinel log base directory is: /home/pi/logs/csp/
INFO: Sentinel log name use pid is: true
```

实际的日志文件在
```
~/logs/csp/sentinel-dashboard.log
```

启动成功后，浏览器访问http://localhost:8080/

尝试添加流控规则，会失败

![添加流控规则失败](https://user-images.githubusercontent.com/15523186/112311192-68939f00-8ce0-11eb-8636-a8ce04d01f09.png)

因为还没有使用Apollo开放平台赋权，sentinel控制台此时没有权限操作Apollo上的namespace，这里显示的是`storage`这个namespace，是sentinel控制台的存储

### 停止

如果有特殊情况，可以使用如下命令停止sentinel控制台

```bash
sh scripts/shutdown.sh
```
## Apollo开放平台授权管理-赋权

此时通过sentinel控制台还不能操作任何应用的流控规则（甚至不能操控自己的）

因为还没有赋权

在Apollo-Portal上，用管理员用户apollo登录

点击右上角的**管理员工具**

点击**开放平台授权管理**

![开放平台授权管理](https://user-images.githubusercontent.com/15523186/112304979-1ac76880-8cd9-11eb-97d4-3715aa8d40d3.png)

输入之前使用的**第三方应用ID**

点击**查询**

![第三方应用ID查询](https://user-images.githubusercontent.com/15523186/112310073-1aca6700-8cdf-11eb-9e89-7fe0e031a32e.png)

可以看到Token显示

赋权

![赋权](https://user-images.githubusercontent.com/15523186/112310632-c5db2080-8cdf-11eb-82ab-ad2365305ed8.png)

被管理的AppId中输入sentinel控制台在Apollo上的appId，也就是`example.sentinel.dashboard`

授权类型选**App**

点击提交

之后针对sentinel控制台自身的流控规则修改，即可生效

尝试添加一条流控规则，再到Apollo上查看sentinel控制台的配置，发现多出了2个namespace

![运行时新建的2个namespace](https://user-images.githubusercontent.com/15523186/112312119-6aaa2d80-8ce1-11eb-823f-ec260adc73e9.png)

## 后续

部署完成后，日常运维、使用请参考[使用/sentinel-dashboard](zh/usage/sentinel-dashboard)