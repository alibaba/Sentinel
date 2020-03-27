# Sentinel Logging Extension Logback

To use Sentinel Logging Extension Logback, you should add the following dependency firstly:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-logging-logback</artifactId>
    <artifactId>sentinel-logging-extension-logback</artifactId>
    <version>x.y.z</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>${logback.version}</version>
</dependency>
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-core</artifactId>
    <version>${logback.version}</version>
</dependency>
```
If you want to use Logback, you can add dependencies of Logback.
Then you should provide logging configuration as specification of the logging framework. 
And you can add Sentinel's Loggers that it name is `sentinelRecordLogger` or `sentinelCommandCenterLogger` for your needs. For example: 

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration scan="false" scanPeriod="60000" debug="false">
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>%-5level %logger - %msg%n</pattern>
        </layout>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>sentinel-record.log</file>
        <append>true</append>
        <encoder>
            <pattern>%-5level %logger - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="FILE2" class="ch.qos.logback.core.FileAppender">
        <file>sentinel-command-center.log</file>
        <append>true</append>
        <encoder>
            <pattern>%-5level %logger - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="sentinelRecordLogger" level="trace">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE" />
    </logger>

    <logger name="sentinelCommandCenterLogger" level="trace">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="FILE2" />
    </logger>

</configuration>
```


