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
package com.taobao.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.nacos.api.config.ConfigService;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.SystemRuleEntity;

import java.util.List;

/**
 * @author Created by YL on 2018/12/27
 */
public class SystemRuleNacosProvider implements BaseDynamicRuleNacosProvider<SystemRuleEntity> {
    private ConfigService configService;
    private Converter<String, List<SystemRuleEntity>> decoder;

    public SystemRuleNacosProvider(ConfigService configService, Converter<String, List<SystemRuleEntity>> decoder) {
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public ConfigService getConfigService() {
        return this.configService;
    }

    @Override
    public Converter<String, List<SystemRuleEntity>> getDecoder() {
        return this.decoder;
    }

    @Override
    public String getPostfix() {
        return NacosConfigUtil.SYSTEM_DATA_ID_POSTFIX;
    }
}
