package com.taobao.csp.sentinel.dashboard.rule.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.fastjson.JSON;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 类名称：ZookeeperConfig
 * 类描述：注册转换类
 * 开发人：朱水平【Tank】
 * 创建时间：2018/12/21.10:20
 * 修改备注：
 *
 * @version 1.0.0
 */
@Configuration
public class ZookeeperConfig {

    @Bean
    public Converter<List<FlowRuleEntity>, String> flowRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<FlowRuleEntity>> flowRuleEntityDecoder() {
        return s -> JSON.parseArray(s, FlowRuleEntity.class);
    }

    @Bean
    public Converter<List<DegradeRuleEntity>, String> degradeRuleEntityEncoder() {
        return JSON::toJSONString;
    }

    @Bean
    public Converter<String, List<DegradeRuleEntity>> degradeRuleEntityDecoder() {
        return s -> JSON.parseArray(s, DegradeRuleEntity.class);
    }

}
