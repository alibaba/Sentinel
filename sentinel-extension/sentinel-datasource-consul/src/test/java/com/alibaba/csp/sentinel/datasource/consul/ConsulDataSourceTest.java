package com.alibaba.csp.sentinel.datasource.consul;


import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class ConsulDataSourceTest {
    private ConsulProcess consul;

    private ConsulClient client;

    private ReadableDataSource<String, List<FlowRule>> consulDataSource;

    private String host = "127.0.0.1";

    private int port;

    private String ruleKey = "sentinel.rules.flow.ruleKey";

    private int waitTimeout = 60;

    private List<FlowRule> rules;

    @Before
    public void init() {
        this.consul = ConsulStarterBuilder.consulStarter()
                .build()
                .start();
        this.port = consul.getHttpPort();
        client = new ConsulClient(host, port);
        Converter<String, List<FlowRule>> flowConfigParser = buildFlowConfigParser();
        String flowRulesJson =
                "[{\"resource\":\"test\", \"limitApp\":\"default\", \"grade\":1, \"count\":\"0.0\", \"strategy\":0, "
                        + "\"refResource\":null, "
                        +
                        "\"controlBehavior\":0, \"warmUpPeriodSec\":10, \"maxQueueingTimeMs\":500, \"controller\":null}]";
        initConsulRuleData(flowRulesJson);
        rules = flowConfigParser.convert(flowRulesJson);
        consulDataSource = new ConsulDataSource<>(host, port, ruleKey, waitTimeout, flowConfigParser);
        FlowRuleManager.register2Property(consulDataSource.getProperty());
    }

    @After
    public void clean() throws Exception {
        if (consulDataSource != null) {
            consulDataSource.close();
        }
        if (consul != null) {
            consul.close();
        }
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
        TimeUnit.MILLISECONDS.sleep(waitTimeout);
        List<FlowRule> rules = FlowRuleManager.getRules();
        Assert.assertEquals(this.rules, rules);
    }


    private Converter<String, List<FlowRule>> buildFlowConfigParser() {
        return source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
    }

    private void initConsulRuleData(String flowRulesJson) {
        Response<Boolean> response = client.setKVValue(ruleKey, flowRulesJson);
        Assert.assertEquals(Boolean.TRUE,  response.getValue());
    }


}