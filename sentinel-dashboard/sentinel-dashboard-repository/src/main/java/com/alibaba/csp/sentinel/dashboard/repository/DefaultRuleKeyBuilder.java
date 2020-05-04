/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.repository;

import com.alibaba.csp.sentinel.dashboard.entity.rule.*;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.springframework.core.ResolvableType;

import java.util.Map;

/**
 * @author cdfive
 */
public class DefaultRuleKeyBuilder<T extends RuleEntity> implements RuleKeyBuilder<T> {

    private final static String RULES = "rules";

    private final static String SEPARATOR = "-";

    private final static Map<Class, String> RULE_NAME_MAP = ImmutableMap.of(
        FlowRuleEntity.class, "flow",
        DegradeRuleEntity.class, "degrade",
        SystemRuleEntity.class, "system",
        AuthorityRuleEntity.class, "authority",
        ParamFlowRuleEntity.class, "paramflow"
    );

    @Override
    public String buildRuleKey(String app, String ip, Integer port) {
        Class<?> clazz = ResolvableType.forClass(getClass()).getSuperType().getGeneric(0).resolve();
        String ruleName = RULE_NAME_MAP.get(clazz);
        return Joiner.on(SEPARATOR).join(app, ip, port, ruleName, RULES);
    }
}
