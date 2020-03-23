package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Properties;

/**
 * @author cdfive
 */
@Configuration
//@ConditionalOnClass
@ConditionalOnProperty(name = "rule.repository.type", havingValue = "nacos")
@EnableConfigurationProperties(NacosProperties.class)
public class NacosConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfig.class);

    @Autowired
    private NacosProperties nacosProperties;

    @Bean
    public ConfigService nacosConfigService() throws Exception {
        String serverAddr = nacosProperties.getServerAddr();
        AssertUtil.notEmpty(serverAddr, "Nacos ConfigService init failed, serverAddr can't be empty");

        LOGGER.info("Nacos ConfigService init start, serverAddr={}", serverAddr);
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        try {
            ConfigService configService = ConfigFactory.createConfigService(properties);
            LOGGER.info("Nacos ConfigService init success");
            return configService;
        } catch (Throwable e) {
            LOGGER.error("Nacos ConfigService init error", e);
            throw e;
        }
    }

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

//    @Bean
//    public FlowRuleNacosProvider flowRuleNacosProvider() {
//        return new FlowRuleNacosProvider();
//    }
}
