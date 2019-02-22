package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.client.exception.ApolloOpenApiException;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/***
 * 配置中心与Sentinel管控台对接入口
 * @author Fx_demon
 */
@Configuration
@ConditionalOnProperty(name = "csp.sentinel.dashboard.dynamic-rule.type", havingValue = "Apollo")
@ConditionalOnClass(ApolloOpenApiClient.class)
public class ApolloDynamicRuleAutoConfiguration {

    @Configuration
    class ApolloConfiguration {
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.server-addr:localhost}")
        private String serverAddr;
        
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.token:000000000}")
        private String token;

        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.readTimeout:1000}")
        private int readTimeout; 
        
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.releaseEnabled:false}")
        private boolean releaseEnabled;
        
        
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.env:DEV}")
        private String env;
        
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.cluster-name:default}")
        private String clusterName;
        
        @Value("${csp.sentinel.dashboard.dynamic-rule.apollo.namespace-name:application}")
        private String namespaceName;
        
        @Bean
        public ApolloOpenApiClient configService() throws ApolloOpenApiException {
            System.out.println("value: " + serverAddr);
            System.out.println("system: " + System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.server-addr"));
            System.setProperty("csp.sentinel.dashboard.dynamic-rule.apollo.server-addr", serverAddr);
            System.out.println("system: " + System.getProperty("csp.sentinel.dashboard.dynamic-rule.apollo.server-addr"));
            
            System.setProperty("csp.sentinel.dashboard.dynamic-rule.apollo.env",env);
        	System.setProperty("csp.sentinel.dashboard.dynamic-rule.apollo.cluster-name",clusterName);
        	System.setProperty("csp.sentinel.dashboard.dynamic-rule.apollo.namespace-name",namespaceName);
        	System.setProperty("csp.sentinel.dashboard.dynamic-rule.apollo.releaseEnabled",releaseEnabled ? "0":"1");
            
            ApolloOpenApiClient client = ApolloOpenApiClient.newBuilder()
                    .withPortalUrl(serverAddr)
                    .withToken(token)
                    .withReadTimeout(readTimeout)
                    .build();
            
			return client; 
        }
    }	
 

    @Configuration
    class FlowRuleConfiguration {
        @Bean
        public Converter<List<FlowRuleEntity>, String> flowEncoder() {
            return JSON::toJSONString;
        }

        @Bean
        public Converter<String, List<FlowRuleEntity>> flowDecoder() {
            return s -> JSON.parseArray(s, FlowRuleEntity.class);
        }

        @Bean
        public FlowRuleApolloProvider flowRuleApolloProvider(ApolloOpenApiClient configService, Converter<String,
                List<FlowRuleEntity>> flowDecoder) {
            return new FlowRuleApolloProvider(configService, flowDecoder);
        }

        @Bean
        public FlowRuleApolloPublisher flowRuleApolloPublisher(ApolloOpenApiClient configService,
                                                             Converter<List<FlowRuleEntity>, String> flowEncoder) {
            return new FlowRuleApolloPublisher(configService, flowEncoder);
        }
    }

    @Configuration
    class AuthorityRuleConfiguration {
        @Bean
        public Converter<List<AuthorityRuleEntity>, String> authorityEncoder() {
            return JSON::toJSONString;
        }

        @Bean
        public Converter<String, List<AuthorityRuleEntity>> authorityDecoder() {
            return s -> JSON.parseArray(s, AuthorityRuleEntity.class);
        }

        @Bean
        public AuthorityRuleApolloProvider authorityRuleApolloProvider(ApolloOpenApiClient configService, Converter<String,
                List<AuthorityRuleEntity>> authorityDecoder) {
            return new AuthorityRuleApolloProvider(configService, authorityDecoder);
        }

        @Bean
        public AuthorityRuleApolloPublisher authorityRuleApolloPublisher(ApolloOpenApiClient configService,
                                                                       Converter<List<AuthorityRuleEntity>, String> authorityEncoder) {
            return new AuthorityRuleApolloPublisher(configService, authorityEncoder);
        }
    }

    @Configuration
    class DegradeRuleConfiguration {
        @Bean
        public Converter<List<DegradeRuleEntity>, String> degradeEncoder() {
            return JSON::toJSONString;
        }

        @Bean
        public Converter<String, List<DegradeRuleEntity>> degradeDecoder() {
            return s -> JSON.parseArray(s, DegradeRuleEntity.class);
        }

        @Bean
        public DegradeRuleApolloProvider degradeRuleApolloProvider(ApolloOpenApiClient configService, Converter<String,
                List<DegradeRuleEntity>> degradeDecoder) {
            return new DegradeRuleApolloProvider(configService, degradeDecoder);
        }

        @Bean
        public DegradeRuleApolloPublisher degradeRuleApolloPublisher(ApolloOpenApiClient configService,
                                                                   Converter<List<DegradeRuleEntity>, String> degradeEncoder) {
            return new DegradeRuleApolloPublisher(configService, degradeEncoder);
        }
    }

    @Configuration
    class ParamFlowRuleConfiguration {
        @Bean
        public Converter<List<ParamFlowRuleEntity>, String> paramEncoder() {
            return JSON::toJSONString;
        }

        @Bean
        public Converter<String, List<ParamFlowRuleEntity>> paramDecoder() {
            return s -> JSON.parseArray(s, ParamFlowRuleEntity.class);
        }

        @Bean
        public ParamFlowRuleApolloProvider paramFlowRuleApolloProvider(ApolloOpenApiClient configService, Converter<String,
                List<ParamFlowRuleEntity>> paramDecoder) {
            return new ParamFlowRuleApolloProvider(configService, paramDecoder);
        }

        @Bean
        public ParamFlowRuleApolloPublisher paramFlowRuleApolloPublisher(ApolloOpenApiClient configService,
                                                                       Converter<List<ParamFlowRuleEntity>, String> paramEncoder) {
            return new ParamFlowRuleApolloPublisher(configService, paramEncoder);
        }
    }

    @Configuration
    class SystemRuleRuleConfiguration {
        @Bean
        public Converter<List<SystemRuleEntity>, String> systemEncoder() {
            return JSON::toJSONString;
        }

        @Bean
        public Converter<String, List<SystemRuleEntity>> systemDecoder() {
            return s -> JSON.parseArray(s, SystemRuleEntity.class);
        }

        @Bean
        public SystemRuleApolloProvider systemRuleApolloProvider(ApolloOpenApiClient configService, Converter<String,
                List<SystemRuleEntity>> systemDecoder) {
            return new SystemRuleApolloProvider(configService, systemDecoder);
        }

        @Bean
        public SystemRuleApolloPublisher systemRuleApolloPublisher(ApolloOpenApiClient configService,
                                                                 Converter<List<SystemRuleEntity>, String> systemEncoder) {
            return new SystemRuleApolloPublisher(configService, systemEncoder);
        }
    }
    
}
	 
