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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.rule;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Eric Zhao
 * @since 0.2.1
 */
public class AuthorityRuleEntity extends AbstractRuleEntity<AuthorityRule> {

    public AuthorityRuleEntity() {}

    public AuthorityRuleEntity(AuthorityRule authorityRule) {
        AssertUtil.notNull(authorityRule, "Authority rule should not be null");
        this.rule = authorityRule;
    }

    public static AuthorityRuleEntity fromAuthorityRule(String app, String ip, Integer port, AuthorityRule rule) {
        AuthorityRuleEntity entity = new AuthorityRuleEntity(rule);
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);
        return entity;
    }

    @JsonIgnore
    public String getLimitApp() {
        return rule.getLimitApp();
    }

    @JsonIgnore
    public String getResource() {
        return rule.getResource();
    }

    @JsonIgnore
    public int getStrategy() {
        return rule.getStrategy();
    }
}
