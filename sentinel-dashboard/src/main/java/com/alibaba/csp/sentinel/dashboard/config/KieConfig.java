package com.alibaba.csp.sentinel.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KieConfig {

    @Value("${kie.config.address}")
    String kieAddress;

    public String getKieBaseUrl(String project){
        return "http://" + kieAddress + "/v1/" + project + "/kie/kv";
    }
}
