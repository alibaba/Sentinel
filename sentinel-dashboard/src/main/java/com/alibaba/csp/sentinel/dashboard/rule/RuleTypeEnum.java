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
package com.alibaba.csp.sentinel.dashboard.rule;

import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;

/**
 * An abstract class between rule type's name and {@link AbstractRule}.
 *
 * {@link GatewayFlowRule} and {@link ApiDefinition} are special because they doesn't inherit from {@link AbstractRule}.
 *
 * @author wxq
 * @see <a href="https://github.com/alibaba/spring-cloud-alibaba/blob/master/spring-cloud-alibaba-starters/spring-cloud-alibaba-sentinel-datasource/src/main/java/com/alibaba/cloud/sentinel/datasource/RuleType.java">com/alibaba/cloud/sentinel/datasource/RuleType</a>
 */
public enum RuleTypeEnum {

    AUTHORITY_RULE(AuthorityRule.class),
    SYSTEM_RULE(SystemRule.class),
    DEGRADE_RULE(DegradeRule.class),
    PARAM_FLOW_RULE(ParamFlowRule.class),
    FLOW_RULE(FlowRule.class),
    GATEWAY_FLOW_RULE(GatewayFlowRule.class),
    API_DEFINITION(ApiDefinition.class),
    ;

    /**
     * concrete java {@link Class}.
     */
    private final Class<?> clazz;

    RuleTypeEnum(Class<?> clazz) {
        this.clazz = clazz;
    }

}
