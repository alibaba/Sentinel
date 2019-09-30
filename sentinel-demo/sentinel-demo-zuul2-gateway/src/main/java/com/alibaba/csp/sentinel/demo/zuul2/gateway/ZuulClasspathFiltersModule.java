package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.endpoint.SentinelZuulEndpoint;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.inbound.SentinelZuulInboundFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul2.filters.outbound.SentinelZuulOutboundFilter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.netflix.zuul.BasicFilterUsageNotifier;
import com.netflix.zuul.DynamicCodeCompiler;
import com.netflix.zuul.FilterFactory;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.guice.GuiceFilterFactory;


public class ZuulClasspathFiltersModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DynamicCodeCompiler.class).to(GroovyCompiler.class);
        bind(FilterFactory.class).to(GuiceFilterFactory.class);

        bind(FilterUsageNotifier.class).to(BasicFilterUsageNotifier.class);

//        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
//
//        bind(ScheduledExecutorService.class).toInstance(scheduledThreadPoolExecutor);
        
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(getConfigInstance().getString("com.claro.config.proxyroutes.jdbc.url"));
//        hikariConfig.setUsername(getConfigInstance().getString("com.claro.config.proxyroutes.jdbc.username"));
//        hikariConfig.setPassword(getConfigInstance().getString("com.claro.config.proxyroutes.jdbc.password"));
//        hikariConfig.addDataSourceProperty("cachePrepStmts","true");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSize","250");
//        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit","2048");
//        DataSource configDatasource = new HikariDataSource(hikariConfig);
        
//        bind(DataSource.class).toInstance(configDatasource);
        
//        HttpClient httpClient = HttpClientBuilder.create()
//        		.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
//        		.build();
//
//        bind(HttpClient.class).toInstance(httpClient);
        
//        bind(ProxyConfigurationDAO.class);
//
//        bind(APIMAuthorizationService.class);
//
//        bind(ProxyConfigurationService.class);
        
        Multibinder<ZuulFilter> filterMultibinder = Multibinder.newSetBinder(binder(), ZuulFilter.class);
        filterMultibinder.addBinding().toInstance(new SentinelZuulInboundFilter(500));
        filterMultibinder.addBinding().toInstance(new SentinelZuulOutboundFilter(500));
        filterMultibinder.addBinding().toInstance(new DemoZuulInboundFilter(1000));
        filterMultibinder.addBinding().toInstance(new SentinelZuulEndpoint());
        filterMultibinder.addBinding().toInstance(new Route());
//        filterMultibinder.addBinding().to(ForwardFilter.class);
//        filterMultibinder.addBinding().to(APIMAuthorizationFilter.class);
//        filterMultibinder.addBinding().to(InboundLoggingFilter.class);
//        filterMultibinder.addBinding().to(OutboundLoggingFilter.class);
        
    }
}
