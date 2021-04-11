# Apollo开放平台

sentinel控制台需要通过Apollo Portal修改应用的规则，使用到了[Apollo开放平台](https://ctripcorp.github.io/apollo/#/zh/usage/apollo-open-api-platform)

这也是通过sentinel控制台，可以对应用的规则进行修改的原理

## 给sentinel控制台创建第三方应用

使用超级管理员账号`apollo`登陆Apollo Portal

管理员工具/开放平台授权管理

填入相关信息

![创建第三方应用](https://user-images.githubusercontent.com/15523186/114275775-5fcbf880-9a56-11eb-904a-45bf57541d05.png)

创建出第三方应用（推荐和Sentinel控制台使用的`AppId`同名）

会得到一个Token，将其填入Sentinel控制台的配置后，Sentinel控制台即可利用这个Token，来修改Apollo上其它项目的配置

## 赋权单个应用

Sentinel控制台在获取到Token后，此时并不能修改任何应用的配置，因为还没有对这个Token进行**赋权**

![赋权](https://user-images.githubusercontent.com/15523186/114275899-dc5ed700-9a56-11eb-9bed-9c08dfcd6e52.png)

填入Token，

**被管理的AppId**填入Sentinel控制台上的应用名

**授权类型**选择**App**

然后点击提交，之后Sentinel控制台就可以对这个应用的规则进行增删查改了

## 批量赋权多个应用

当存在多个AppId需要被管理时，每次赋权一个被管理的AppId比较麻烦

于是考虑是否存在批量赋权多个应用的方法

### 使用Sentinel控制台（推荐）

> 从版本1.8.1-2-RELEASE开始

为了更加方便地使用，在Sentinel控制台的首页，提供了一个便捷的方式，来完成针对多个应用的**赋权**操作，

需要使用Apollo Portal在超级管理员登录后的Cookie

参考[使用/Sentinel控制台](zh/usage/sentinel-dashboard)

### 使用现成的Shell Script脚本

这里提供现成的shell脚本，通过外部配置，可以直接使用

在.zip文件中，目录`scripts/authorization`

存在以下文件

| 文件名          | 作用                                |
| --------------- | ----------------------------------- |
| authorize.sh    | 要运行的程序，赋权多个被管理的AppId |
| variables.sh    | 赋权过程中使用到的环境变量          |
| application.txt | 存放被管理的AppId，每个占一行       |

可以通过如下命令进行使用

```bash
export JSESSIONID=2443D86C91338190807F1818319104EC
export APOLLO_PORTAL_IP=106.54.227.205
export APOLLO_PORTAL_PORT=8070
export APOLLO_OPEN_API_TOKEN=58bb334437c20e55b86329d4238cd5baf4d4d420
sh scripts/authorization/authorize.sh
```

运行后，会在目录`scripts/authorization/logs`下生成日志，每个AppId会有对应的一个`AppId.log`文件，来记录这次curl命令的输出

### 原理

发现可以使用HTTP POST来实现

举个例子，对于赋权单个应用，HTTP POST请求URL

http://106.54.227.205/consumers/a610c865a7fae479cb40549f8cae540788f7d5f4/assign-role?type=AppRole

请求体

```json
{"appId":"sentinel.dashboard"}
```

所以是否可以手动发送HTTP请求，实现赋权多个应用？

可以用curl命令来实现这个需求（用Java程序也可，考虑到Linux服务器下，shell script用起来更方便）

但是我们知道，如果要给Apollo Portal发送这个请求，需要先登录

所以可以在浏览器上登录，然后将Cookie复制出来

使用Chrome浏览器，用管理员用户apollo登录后，点击左上方的不安全

![不安全](https://user-images.githubusercontent.com/15523186/113005150-cd4e6e00-91a6-11eb-8cb9-630dcf7fd23b.png)

点击Cookie

![Cookie](https://user-images.githubusercontent.com/15523186/113005183-d5a6a900-91a6-11eb-8d83-81d71ba6b64b.png)

选中JSESSIONID，然后复制它的内容

![复制](https://user-images.githubusercontent.com/15523186/113005280-ec4d0000-91a6-11eb-9b09-e5dfab7bae50.png)

即可将Cookie信息复制出来

然后将Cookie信息，放入curl命令的参数中即可

例如

apollo portal的IP为106.54.227.205，端口为8070

JSESSIONID=2443D86C91338190807F1818319104EC

要被管理的AppId为test

Token是58bb334437c20e55b86329d4238cd5baf4d4d420

可以得到如下命令

```bash
curl -vs --request POST --header 'Cookie: JSESSIONID=C988945BC51E13689EA7517AB1194972' --header 'Content-Type: application/json;charset=UTF-8' --data-raw '{"appId":"test"}' 'http://106.54.227.205:8070/consumers/58bb334437c20e55b86329d4238cd5baf4d4d420/assign-role?type=AppRole'
```



