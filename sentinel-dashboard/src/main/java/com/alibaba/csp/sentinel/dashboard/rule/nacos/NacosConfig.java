package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Properties;

@Component
public class NacosConfig {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:}")
    private String namespace;

    @Value("${spring.cloud.nacos.discovery.username:}")
    private String username;

    @Value("${spring.cloud.nacos.discovery.password:}")
    private String password;

    private ConfigService configService;

    @PostConstruct
    public void init() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        if (namespace != null && !namespace.isEmpty()) {
            properties.setProperty(PropertyKeyConst.NAMESPACE, namespace);
        }
        properties.setProperty(PropertyKeyConst.USERNAME, username);
        properties.setProperty(PropertyKeyConst.PASSWORD, password);
        configService = ConfigFactory.createConfigService(properties);
    }

    public ConfigService getConfigService() {
        return configService;
    }
}
