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
package com.alibaba.csp.sentinel.datasource.spring.cloud.config;

import org.springframework.core.env.PropertySource;

/**
 * Storage data pull from spring-config-cloud server
 * And notice ${@link SpringCloudConfigDataSource} update latest values
 *
 * @author lianglin
 * @since 1.7.0
 */
public class SentinelRuleStorage {

    public static PropertySource<?> rulesSource;

    public static void setRulesSource(PropertySource<?> source) {
        rulesSource = source;
        noticeSpringCloudDataSource();
    }

    public static String retrieveRule(String ruleKey) {
        return rulesSource == null ? null : (String) rulesSource.getProperty(ruleKey);
    }

    private static void noticeSpringCloudDataSource(){
        SpringCloudConfigDataSource.updateValues();
    }

}
