# Sentinel Web Servlet Filter

Sentinel provides Servlet filter integration. To use the filter,
you can simply configure your `web.xml` with:

```xml
<filter>
	<filter-name>SentinelCommonFilter</filter-name>
	<filter-class>com.alibaba.csp.sentinel.adapter.servlet.CommonFilter</filter-class>
</filter>

<filter-mapping>
	<filter-name>SentinelCommonFilter</filter-name>
	<url-pattern>/*</url-pattern>
</filter-mapping>
```

When a request is blocked, Sentinel servlet filter will give a default page indicating the request blocked.
If customized block page is set (via `WebServletConfig.setBlockPage(blockPage)` method),
the filter will redirect the request to provided URL. You can also implement your own
block handler (the `UrlBlockHandler` interface) and register to `WebCallbackManager`.

