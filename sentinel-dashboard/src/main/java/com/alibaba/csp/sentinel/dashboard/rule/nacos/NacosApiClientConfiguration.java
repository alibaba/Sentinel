package com.alibaba.csp.sentinel.dashboard.rule.nacos;


import com.alibaba.csp.sentinel.dashboard.rule.PersistentRuleApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
@Configuration
@ConditionalOnBean(name = "nacosConfigService")
public class NacosApiClientConfiguration {

    @Bean
    public PersistentRuleApiClient persistentApiClient(){
        return new NacosApiClient();
    }

}
