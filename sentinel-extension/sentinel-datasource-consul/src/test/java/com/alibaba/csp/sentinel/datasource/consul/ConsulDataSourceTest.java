/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.datasource.consul;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import org.junit.*;
import org.testcontainers.consul.ConsulContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author wavesZh
 */
public class ConsulDataSourceTest {
    @ClassRule
    public static ConsulContainer consulContainer = new ConsulContainer("hashicorp/consul:1.15");

    private final String ruleKey = "sentinel.rules.flow.ruleKey";
    private final int waitTimeoutInSecond = 1;

    private ConsulClient client;

    private ReadableDataSource<String, List<FlowRule>> consulDataSource;

    private List<FlowRule> rules;

    @Before
    public void init() {
        int port = consulContainer.getMappedPort(8500);
        String host = consulContainer.getHost();
        client = new ConsulClient(host, port);
        Converter<String, List<FlowRule>> flowConfigParser = buildFlowConfigParser();
        String flowRulesJson =
            "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, "
                + "\"refResource\":null, "
                +
                "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":500, \"controller\":null}]";
        initConsulRuleData(flowRulesJson);
        rules = flowConfigParser.convert(flowRulesJson);
        consulDataSource = new ConsulDataSource<>(host, port, ruleKey, waitTimeoutInSecond, flowConfigParser);
        FlowRuleManager.register2Property(consulDataSource.getProperty());
    }

    @After
    public void clean() throws Exception {
        if (consulDataSource != null) {
            consulDataSource.close();
        }
        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testConsulDataSourceWhenInit() {
        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertEquals(this.rules, rules);
    }

    @Test
    public void testConsulDataSourceWhenUpdate() throws InterruptedException {
        rules.get(0).setMaxQueueingTimeMs(new Random().nextInt());
        client.setKVValue(ruleKey, JSON.toJSONString(rules));
        TimeUnit.SECONDS.sleep(waitTimeoutInSecond);
        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertEquals(this.rules, rules);
    }

    private Converter<String, List<FlowRule>> buildFlowConfigParser() {
        return source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
    }

    private void initConsulRuleData(String flowRulesJson) {
        Response<Boolean> response = client.setKVValue(ruleKey, flowRulesJson);
        Assert.assertEquals(Boolean.TRUE, response.getValue());
    }

}
