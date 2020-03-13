package com.alibaba.csp.sentinel.demo.zuul2.gateway;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.providers.MyDataCenterInstanceConfigProvider;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.InjectorBuilder;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.Server;

public class Bootstrap {
    public static void main(String[] args) {
        new Bootstrap().start();
    }

    public void start() {
        Server server;
        try {
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

    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("another_customized_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(1)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pa")
                )
        );
        rules.add(new GatewayFlowRule("some_customized_api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                .setCount(5)
                .setIntervalSec(1)
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                        .setFieldName("pn")
                )
        );
        GatewayRuleManager.loadRules(rules);
    }

    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition("some_customized_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/ahas"));
                    add(new ApiPathPredicateItem().setPattern("/aliyun/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        ApiDefinition api2 = new ApiDefinition("another_customized_api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(api1);
        definitions.add(api2);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
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
