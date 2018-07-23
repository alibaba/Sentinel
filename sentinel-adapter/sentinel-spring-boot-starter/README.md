# Sentinel Spring Boot Starter

Sentinel Spring Boot Starter provides out-of-box integration with Spring Boot applications
(e.g. web applications, Dubbo services).

## Web Servlet

Web servlet integration is enabled by default. You need to configure URL patterns in your config file (e.g. properties file):

```
spring.sentinel.servletFilter.urlPatterns=/*
```

By default the URL pattern is `/*`.

## Dubbo

Dubbo integration is enabled by default. You need to disable the filters manually if you don't want them.