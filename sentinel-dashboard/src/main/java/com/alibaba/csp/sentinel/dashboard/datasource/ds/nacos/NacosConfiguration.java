/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.datasource.ds.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.ds.DataSourceProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @Author Jiajiangnan
 * @E-mail jiajiangnan.office@foxmail.com
 * @Date 2020/8/30
 * @since 1.8.0
 */
@Configuration
@ConditionalOnProperty(prefix = DataSourceProperties.PREFIX_DATASOURCE, name = DataSourceProperties.NAME_PROVIDER, havingValue = DataSourceProperties.VALUE_PROVIDER_NACOS, matchIfMissing = false)
public class NacosConfiguration {

    @Autowired
    private NacosProperties nacosProperties;

    @Bean
    public ConfigService nacosConfigService() throws Exception {

        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosProperties.getServerAddr());
        if(StringUtils.isNotBlank(nacosProperties.getUsername())) {
            properties.put(PropertyKeyConst.USERNAME, nacosProperties.getUsername());
        }
        if(StringUtils.isNotBlank(nacosProperties.getPassword())) {
            properties.put(PropertyKeyConst.PASSWORD, nacosProperties.getPassword());
        }
        if(StringUtils.isNotBlank(nacosProperties.getNamespace())) {
            properties.put(PropertyKeyConst.NAMESPACE, nacosProperties.getNamespace());
        }
        return ConfigFactory.createConfigService(properties);
    }
}
