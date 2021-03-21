# 快速开始

## 启动sentinel-dashboard

```bash
git clone https://github.com/Anilople/Sentinel.git
cd Sentinel/sentinel-dashboard
mvn clean package -DskipTests
cd target
java -jar sentinel-dashboard*.jar
```

访问

http://localhost:8080

并登录

用户名和密码都是

```
sentinel
```

## 启动sentinel-demo-apollo

```bash
git clone https://github.com/Anilople/sentinel-demo-apollo.git
cd sentinel-demo-apollo
mvn clean package -DskipTests
cd target
java -jar sentinel-demo-apollo.jar
```

访问
http://localhost:8081/echo?id=3
多点击几次

然后可以看到dashboard上显示

![sentinel demo apollo](https://user-images.githubusercontent.com/15523186/110650906-eb532f00-81f5-11eb-8bb7-10ee3cf81100.png)

接下来可以对`sentinel.demo.apollo`编辑流控规则、降级规则、热点规则、系统规则、授权规则

相关的信息会持久化到Apollo配置中心里面

查看对`sentinel.demo.apollo`的配置是否生效，可以通过

* WebUI: dashboard
* actuator监控: http://localhost:8081/actuator/sentinel