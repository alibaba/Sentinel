# Sentinel Logging Extension SLF4J

To integrate logs of sentinel into your project which uses slf4j for bridge of logging you can
simply introduce following dependency to your project:

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-logging-slf4j</artifactId>
    <version>${sentinel.version}</version>
</dependency>
```

And if you want to control level of logging special for sentinel the loggers that sentinel uses
are called `sentinelRecordLogger` and `sentinelCommandCenterLogger`. For example in XML configration
coming with log4j2 implementation:

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
