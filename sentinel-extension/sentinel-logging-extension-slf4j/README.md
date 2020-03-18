# Sentinel Logging Extension SLF4J

To use Sentinel Logging Extension SLF4J with Log4j2, you should add the following dependency firstly:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-logging-extension-slf4j</artifactId>
    <version>x.y.z</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>${slf4j.version}</version>
</dependency>
```
If you want to use Slf4j with Log4j2, you can add dependencies of Log4j2 and the binding about Log4j2 and SLF4J.
Then you should provide logging configuration as specification of the logging framework. 
And you can add Sentinel's Loggers that it name is `sentinelRecordLogger` or `sentinelCommandCenterLogger` for your needs. For example: 

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%-5level %logger - %msg%n"/>
        </Console>
        <File name="FILE" fileName="sentinel-record.log" append="false">
            <PatternLayout pattern="%-5level %logger - %msg%n"/>
        </File>
        <File name="FILE2" fileName="sentinel-command-center.log" append="false">
            <PatternLayout pattern="%-5level %logger - %msg%n"/>
        </File>
    </Appenders>
    <Loggers>
        <Root level="info"/>
        <logger name="sentinelRecordLogger" level="trace">
            <appender-ref ref="Console" />
            <appender-ref ref="FILE" />
        </logger>
        <logger name="sentinelCommandCenterLogger" level="trace">
            <appender-ref ref="Console" />
            <appender-ref ref="FILE2" />
        </logger>
    </Loggers>
</Configuration>
```


