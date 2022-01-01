package com.alibaba.csp.sentinel.dashboard.config;

import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.*;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.mem.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Configuration
@ConditionalOnProperty(prefix = "rule",name = "provider",havingValue = "mem",matchIfMissing = true)
public class RuleProviderConfig {
    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Bean
    public DynamicRuleProvider flowRuleProvider(){
        return new MemRuleProvider<>(m ->
                sentinelApiClient.fetchFlowRuleOfMachine(m.getApp(),
                        m.getHostname(), m.getIp(), m.getPort()));
    }

    @Bean
    public DynamicRulePublisher flowRulePublisher(){
        return new MemRulePublisher<List<FlowRuleEntity>>((machine, rules)->
                sentinelApiClient.setFlowRuleOfMachine(machine.getApp(),
                        machine.getIp(), machine.getPort(), rules));
    }

    @Bean
    public DynamicRuleProvider degradeRuleProvider(){
        return new MemRuleProvider<>(m ->
                sentinelApiClient.fetchDegradeRuleOfMachine(m.getApp(),
                        m.getHostname(), m.getIp(), m.getPort()));
    }

    @Bean
    public DynamicRulePublisher degradeRulePublisher(){
        return new MemRulePublisher<List<DegradeRuleEntity>>((machine, rules)->
                sentinelApiClient.setDegradeRuleOfMachine(machine.getApp(),
                        machine.getIp(), machine.getPort(), rules));
    }

    @Bean
    public DynamicRuleProvider paramFlowRuleProvider(){
        return new MemRuleProvider<>(m->{
            try {
                return sentinelApiClient.fetchParamFlowRulesOfMachine(m.getApp(),
                        m.getHostname(), m.getIp(), m.getPort()).get();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        });
    }

    @Bean
    public DynamicRulePublisher paramFlowRulePublisher(){
        return new MemRulePublisher<List<ParamFlowRuleEntity>>((machine, rules)->
                sentinelApiClient.setParamFlowRuleOfMachine(machine.getApp(),
                        machine.getIp(), machine.getPort(), rules));
    }

    @Bean
    public DynamicRuleProvider systemRuleProvider(){
        return new MemRuleProvider<>(m ->
                sentinelApiClient.fetchSystemRuleOfMachine(m.getApp(),
                        m.getHostname(), m.getIp(), m.getPort()));
    }

    @Bean
    public DynamicRulePublisher systemRulePublisher(){
        return new MemRulePublisher<List<SystemRuleEntity>>((machine, rules)->
                sentinelApiClient.setSystemRuleOfMachine(machine.getApp(),
                        machine.getIp(), machine.getPort(), rules));
    }

    @Bean
    public DynamicRuleProvider authorityRuleProvider(){
        return new MemRuleProvider<>(m ->
                sentinelApiClient.fetchAuthorityRulesOfMachine(m.getApp(),
                        m.getHostname(), m.getIp(), m.getPort()));
    }

    @Bean
    public DynamicRulePublisher authorityRulePublisher(){
        return new MemRulePublisher<List<AuthorityRuleEntity>>((machine, rules)->
                sentinelApiClient.setAuthorityRuleOfMachine(machine.getApp(),
                        machine.getIp(), machine.getPort(), rules));
    }

}
