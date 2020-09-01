package com.alibaba.csp.sentinel.dashboard.datasource.ds;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
@Component
public class DataSourceProperties {

    public static final String PREFIX_DATASOURCE = "datasource";
    public static final String NAME_PROVIDER = "provider";

    public static final String VALUE_PROVIDER_MEMORY = "memory";
    public static final String VALUE_PROVIDER_APOLLO = "apollo";
    public static final String VALUE_PROVIDER_NACOS = "nacos";
    public static final String VALUE_PROVIDER_ZOOKEEPER = "zookeeper";

    @Value("${datasource.provider:memory}")
    private String instance;

    public String getInstance() {
        return instance;
    }
    public void setInstance(String instance) {
        this.instance = instance;
    }

}
