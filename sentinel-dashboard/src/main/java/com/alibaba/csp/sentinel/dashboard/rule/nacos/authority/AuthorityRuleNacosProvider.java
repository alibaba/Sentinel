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
package com.alibaba.csp.sentinel.dashboard.rule.nacos.authority;

import com.alibaba.cloud.sentinel.datasource.RuleType;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.RuleNacosProviderAdapter;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;


/**
 * @author Eric Zhao
 * @since 1.4.0
 */
@Component
public class AuthorityRuleNacosProvider extends RuleNacosProviderAdapter<AuthorityRuleEntity> {

    public AuthorityRuleNacosProvider() {
        super.setConverter(source -> JSON.parseArray(source,AuthorityRuleEntity.class));
        super.setRuleType(RuleType.AUTHORITY);
    }
}
