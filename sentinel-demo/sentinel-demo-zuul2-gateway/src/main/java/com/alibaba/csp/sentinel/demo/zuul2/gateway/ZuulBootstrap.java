/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import java.io.IOException;

import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.providers.MyDataCenterInstanceConfigProvider;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.InjectorBuilder;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.Server;

/**
 * <p>The Zuul 2.x demo with Sentinel gateway flow control.</p>
 * <p>Run with {@code -Dcsp.sentinel.api.type=1} to mark the demo as API gateway.</p>
 *
 * @author wavesZh
 */
public class ZuulBootstrap {

    public static void main(String[] args) {
        new ZuulBootstrap().start();
    }

    public void start() {
        Server server;
        try {
            // Load sample rules. You may also manage rules in Sentinel dashboard.
            new GatewayRuleConfig().doInit();

            ConfigurationManager.loadCascadedPropertiesFromResources("application");
            Injector injector = InjectorBuilder.fromModule(new ZuulModule()).createInjector();
            injector.getInstance(FiltersRegisteringService.class);
            BaseServerStartup serverStartup = injector.getInstance(BaseServerStartup.class);
            server = serverStartup.server();
            server.start(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ZuulModule extends ZuulSampleModule {
        @Override
        protected void configure() {
            //DataCenterInfo
            bind(EurekaInstanceConfig.class)
                .toProvider(MyDataCenterInstanceConfigProvider.class)
                .in(Scopes.SINGLETON);
            super.configure();
        }
    }
}
