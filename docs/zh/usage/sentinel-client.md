# sentinel客户端

Spring Boot和Spring Cloud推荐直接使用[Spring Cloud Alibaba Sentinel](https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel)

得益于[SentinelAutoConfiguration](https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-starters/spring-cloud-starter-alibaba-sentinel/src/main/java/com/alibaba/cloud/sentinel/custom/SentinelAutoConfiguration.java)，Spring Boot应用可以快速集成sentinel

## maven依赖

### 版本选择

根据你使用的Spring Boot版本，到

https://mvnrepository.com/artifact/com.alibaba.cloud/spring-cloud-starter-alibaba-sentinel

中寻找合适的版本，spring-cloud-starter-alibaba-sentinel的版本和Spring Boot版本有关联，如下表格

| spring-cloud-starter-alibaba-sentinel | 适配的Spring Boot版本 |
| ------------------------------------- | --------------------- |
| **2.0**.x                             | **2.0**.x             |

TODO，更多适配的版本信息待补充
### dependencyManagement

为了方便管理spring-cloud-starter-alibaba-sentinel的版本，推荐使用maven的依赖管理

```xml
	<dependencyManagement>
		<dependencies>
			<!-- https://mvnrepository.com/artifact/com.alibaba.cloud/spring-cloud-alibaba-dependencies -->
			<dependency>
				<groupId>com.alibaba.cloud</groupId>
				<artifactId>spring-cloud-alibaba-dependencies</artifactId>
				<version>${spring-cloud-alibaba-dependencies.version}</version>
				<scope>import</scope>
				<type>pom</type>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

### dependencies

由于使用了Apollo配置中心，所以Apollo的依赖也需要添加

```xml
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-datasource-apollo</artifactId>
        </dependency>
	</dependencies>
```

### 示例

假设你的Spring Boot版本为`2.0.9.RELEASE`

请到https://mvnrepository.com/artifact/com.alibaba.cloud/spring-cloud-starter-alibaba-sentinel

上面查询2.0.x系列的最新版本，截至2021年3月22日，2.0.x系列的最新版本是[2.0.4.RELEASE](https://mvnrepository.com/artifact/com.alibaba.cloud/spring-cloud-starter-alibaba-sentinel/2.0.4.RELEASE)

添加依赖管理

```xml
	<dependencyManagement>
		<dependencies>
			<!-- https://mvnrepository.com/artifact/com.alibaba.cloud/spring-cloud-alibaba-dependencies -->
			<dependency>
				<groupId>com.alibaba.cloud</groupId>
				<artifactId>spring-cloud-alibaba-dependencies</artifactId>
				<version>2.0.4.RELEASE</version>
				<scope>import</scope>
				<type>pom</type>
			</dependency>
		</dependencies>
	</dependencyManagement>
```

添加依赖

```xml
    <dependencies>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-datasource-apollo</artifactId>
        </dependency>
	</dependencies>
```

即可

## 使用Apollo公共namespace

对应用来说，只需要在`apollo.bootstrap.namespaces`中额外添加一个公共的namespace即可

假设sentinel-dashboard给出的公共namespace是`PT.sentinel`，应用原先的`apollo.bootstrap.namespaces`配置为

```properties
apollo.bootstrap.namespaces = application,FX.apollo,application.yml
```

只需要改成

```properties
apollo.bootstrap.namespaces = application,FX.apollo,application.yml,PT.sentinel
```

然后启动即可

启动成功之后，访问[sentinel-dashboard](zh/usage/sentinel-dashboard)即可看到应用的信息，控制台上显示的应用名对应应用的`app.id`

此时从控制台上尝试给应用添加流控规则，是会失败的，因为控制台并没有权限操作应用的配置

需要赋权（在Apollo Portal上操作）

控制台得到权限后，才可以增删查改应用的流控规则