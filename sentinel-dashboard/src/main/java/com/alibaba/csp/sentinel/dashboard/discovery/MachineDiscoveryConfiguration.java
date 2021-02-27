package com.alibaba.csp.sentinel.dashboard.discovery;


import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MachineDiscoveryConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public SimpleMachineDiscovery simpleMachineDiscovery(){
        return new SimpleMachineDiscovery();
    }

    @Bean
    @ConditionalOnMissingBean
    public AppManagement appManagement(SimpleMachineDiscovery simpleMachineDiscovery){
        return new AppManagement(simpleMachineDiscovery);
    }

}
