package com.taobao.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Created by YL on 2018/12/27
 */
@Configuration
@ConditionalOnProperty(name = "csp.sentinel.dashboard.dynamic-rule.type", havingValue = "nacos")
@ConditionalOnClass(ConfigService.class)
public class NacosDynamicRuleAutoConfiguration {

    @Configuration
    class NacosConfiguration {
        @Value("${csp.sentinel.dashboard.dynamic-rule.nacos.server-addr:localhost}")
        private String serverAddr;

        @Bean
        public ConfigService configService() throws NacosException {
            return ConfigFactory.createConfigService(serverAddr);
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
        public FlowRuleNacosProvider flowRuleNacosProvider(ConfigService configService, Converter<String,
                List<FlowRuleEntity>> flowDecoder) {
            return new FlowRuleNacosProvider(configService, flowDecoder);
        }

        @Bean
        public FlowRuleNacosPublisher flowRuleNacosPublisher(ConfigService configService,
                                                             Converter<List<FlowRuleEntity>, String> flowEncoder) {
            return new FlowRuleNacosPublisher(configService, flowEncoder);
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
        public AuthorityRuleNacosProvider authorityRuleNacosProvider(ConfigService configService, Converter<String,
                List<AuthorityRuleEntity>> authorityDecoder) {
            return new AuthorityRuleNacosProvider(configService, authorityDecoder);
        }

        @Bean
        public AuthorityRuleNacosPublisher authorityRuleNacosPublisher(ConfigService configService,
                                                                       Converter<List<AuthorityRuleEntity>, String> authorityEncoder) {
            return new AuthorityRuleNacosPublisher(configService, authorityEncoder);
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
        public DegradeRuleNacosProvider degradeRuleNacosProvider(ConfigService configService, Converter<String,
                List<DegradeRuleEntity>> degradeDecoder) {
            return new DegradeRuleNacosProvider(configService, degradeDecoder);
        }

        @Bean
        public DegradeRuleNacosPublisher degradeRuleNacosPublisher(ConfigService configService,
                                                                   Converter<List<DegradeRuleEntity>, String> degradeEncoder) {
            return new DegradeRuleNacosPublisher(configService, degradeEncoder);
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
        public ParamFlowRuleNacosProvider paramFlowRuleNacosProvider(ConfigService configService, Converter<String,
                List<ParamFlowRuleEntity>> paramDecoder) {
            return new ParamFlowRuleNacosProvider(configService, paramDecoder);
        }

        @Bean
        public ParamFlowRuleNacosPublisher paramFlowRuleNacosPublisher(ConfigService configService,
                                                                       Converter<List<ParamFlowRuleEntity>, String> paramEncoder) {
            return new ParamFlowRuleNacosPublisher(configService, paramEncoder);
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
        public SystemRuleNacosProvider systemRuleNacosProvider(ConfigService configService, Converter<String,
                List<SystemRuleEntity>> systemDecoder) {
            return new SystemRuleNacosProvider(configService, systemDecoder);
        }

        @Bean
        public SystemRuleNacosPublisher systemRuleNacosPublisher(ConfigService configService,
                                                                 Converter<List<SystemRuleEntity>, String> systemEncoder) {
            return new SystemRuleNacosPublisher(configService, systemEncoder);
        }
    }
}
