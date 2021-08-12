# Sentinel Mybatis Interceptor

Sentinel provides Mybatis Interceptor integration to enable flow control for database requests.

Add the following dependency in `pom.xml` (if you are using Maven)
```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-mybatis-adapter</artifactId>
    <version>x.y.z</version>
</dependency>
```

Configure interceptor, there are six interceptors to choose.

```java
@Configuration
public class InterceptorConfig {
    @Bean
    public SentinelReadInterceptor newReadInterceptor() {
        return new SentinelReadInterceptor();
    }
    @Bean
    public SentinelWriteInterceptor newWriteInterceptor() {
        return new SentinelWriteInterceptor();
    }
    @Bean
    public SentinelTotalInterceptor newTotalInterceptor() {
        return new SentinelTotalInterceptor();
    }
    @Bean
    public SentinelMapperInterceptor newSentinelInterceptor() {
        return new SentinelMapperInterceptor();
    }
    @Bean
    public SentinelSqlInterceptor newSentinelSqlInterceptor() {
        return new SentinelSqlInterceptor();
    }
    @Bean
    public SentinelCommandTypeInterceptor newSentinelCommandTypeInterceptor() {
        return new SentinelCommandTypeInterceptor();
    }
}
```

Custom configuration

- Clean resource name in `SentinelMapperInterceptor`

```java
@Bean
public SentinelMapperInterceptor newSentinelInterceptor() {
    return new SentinelMapperInterceptor(new ResourceNameCleaner() {
        @Override
        public String clean(String resourceName) {
            //resourceName is: com.alibaba.csp.sentinel.demo.mybatis.mapper.UserMapper.getById
            //Clean result is: demo.mybatis.mapper.UserMapper.getById
            return resourceName.replace("com.alibaba.csp.sentinel.", "");
        }
    });
}
```

- `SentinelReadInterceptor` is database read flow control.

- `SentinelWriteInterceptor` is database write flow control.

- `SentinelTotalInterceptor` is database total flow control.

- `SentinelSqlInterceptor` is `Mybatis` sql flow control(e.g. `SELECT * FROM user`).

- `SentinelCommandTypeInterceptor` is `Mybatis` command type flow control(e.g. `mybatis-command-type-SELECT`), command type: `org.apache.ibatis.mapping.SqlCommandType`.

- `SentinelMapperInterceptor` is `Mybatis` Mapper Interface flow control, you can configure `ResourceNameCleaner` in `SentinelMapperInterceptor`.

- `ResourceNameCleaner` can clean resource name, for example:

    1. `com.alibaba.csp.sentinel.demo.mybatis.mapper.UserMapper.getById` -> `UserMapper.getById`

    2. `UserMapper.getById` and `UserMapper.getByName` -> `UserMapper.getBy*`, avoid the amount of context and  will exceed the threshold.

    3. If you need to exclude some resources (that should not be recorded as Sentinel resources),  you may unify the unwanted resources to the empty string "" or null.


