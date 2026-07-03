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
package com.alibaba.csp.sentinel.dashboard.datasource.entity;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowClusterConfig;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author lianglin
 * @since 1.7.0
 */
public class JsonSerializeTest {

    @Test
    public void authorityRuleJsonSerializeTest() {
        AuthorityRuleEntity emptyRule = new AuthorityRuleEntity();
        Assert.assertTrue("{}".equals(JSON.toJSONString(emptyRule)));

        AuthorityRuleEntity authorityRule = new AuthorityRuleEntity();
        AuthorityRule rule = new AuthorityRule();
        rule.setStrategy(0).setLimitApp("default").setResource("rs");
        authorityRule.setRule(rule);
        Assert.assertTrue("{\"rule\":{\"limitApp\":\"default\",\"resource\":\"rs\",\"strategy\":0}}".equals(JSON.toJSONString(authorityRule)));
    }

    @Test
    public void paramFlowRuleSerializeTest() {
        ParamFlowRuleEntity emptyRule = new ParamFlowRuleEntity();
        Assert.assertTrue("{}".equals(JSON.toJSONString(emptyRule)));

        ParamFlowRuleEntity paramFlowRule = new ParamFlowRuleEntity();
        ParamFlowRule rule = new ParamFlowRule();
        rule.setClusterConfig(new ParamFlowClusterConfig());
        rule.setResource("rs").setLimitApp("default");
        paramFlowRule.setRule(rule);
        Assert.assertTrue("{\"rule\":{\"burstCount\":0,\"clusterConfig\":{\"fallbackToLocalWhenFail\":false,\"sampleCount\":10,\"thresholdType\":0,\"windowIntervalMs\":1000},\"clusterMode\":false,\"controlBehavior\":0,\"count\":0.0,\"durationInSec\":1,\"grade\":1,\"limitApp\":\"default\",\"maxQueueingTimeMs\":0,\"paramFlowItemList\":[],\"resource\":\"rs\"}}"
                .equals(JSON.toJSONString(paramFlowRule)));

    }

}
