package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import com.google.inject.AbstractModule;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.BasicRequestCompleteHandler;
import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.context.ZuulSessionContextDecorator;
import com.netflix.zuul.init.ZuulFiltersModule;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.ClientRequestReceiver;
import com.netflix.zuul.origins.BasicNettyOriginManager;
import com.netflix.zuul.origins.OriginManager;
import com.netflix.zuul.stats.BasicRequestMetricsPublisher;
import com.netflix.zuul.stats.RequestMetricsPublisher;

/**
 * Zuul Sample Module
 *
 * Author: Arthur Gonigberg
 * Date: November 20, 2017
 */
public class ZuulSampleModule extends AbstractModule {
    @Override
    protected void configure() {
        // sample specific bindings
        bind(BaseServerStartup.class).to(SampleServerStartup.class);

        // use provided basic netty origin manager
        bind(OriginManager.class).to(BasicNettyOriginManager.class);

        // zuul filter loading
        install(new ZuulFiltersModule());
        bind(FilterFileManager.class).asEagerSingleton();

        install(new ZuulClasspathFiltersModule());
        // general server bindings
        // health/discovery status
        bind(ServerStatusManager.class);
        // decorate new sessions when requests come in
        bind(SessionContextDecorator.class).to(ZuulSessionContextDecorator.class);
        // atlas metrics registry
        bind(Registry.class).to(DefaultRegistry.class);
        // metrics post-request completion
        bind(RequestCompleteHandler.class).to(BasicRequestCompleteHandler.class);
        // discovery client
        bind(AbstractDiscoveryClientOptionalArgs.class).to(DiscoveryClient.DiscoveryClientOptionalArgs.class);
        // timings publisher
        bind(RequestMetricsPublisher.class).to(BasicRequestMetricsPublisher.class);

        // access logger, including request ID generator
        bind(AccessLogPublisher.class).toInstance(new AccessLogPublisher("ACCESS",
                (channel, httpRequest) -> ClientRequestReceiver.getRequestFromChannel(channel).getContext().getUUID()));
    }
}
