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
package com.alibaba.csp.sentinel.datasource.etcd;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


/**
 * @author lianglin
 * @since 1.7.0
 */
@Ignore(value = "Before run this test, you need to set up your etcd server.")
public class EtcdDataSourceTest {


    private final String endPoints = "http://127.0.0.1:2379";


    @Before
    public void setUp() {
        SentinelConfig.setConfig(EtcdConfig.END_POINTS, endPoints);
        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @After
    public void tearDown() {
        SentinelConfig.setConfig(EtcdConfig.END_POINTS, "");
        FlowRuleManager.loadRules(new ArrayList<>());
    }

    @Test
    public void testReadSource() throws Exception {
        EtcdDataSource dataSource = new EtcdDataSource("foo", value -> value);
        KV kvClient = Client.builder()
                .endpoints(endPoints)
                .build().getKVClient();

        kvClient.put(ByteSequence.from("foo".getBytes()), ByteSequence.from("test".getBytes()));
        Assert.assertNotNull(dataSource.readSource().equals("test"));

        kvClient.put(ByteSequence.from("foo".getBytes()), ByteSequence.from("test2".getBytes()));
        Assert.assertNotNull(dataSource.getProperty().equals("test2"));
    }

    @Test
    public void testDynamicUpdate() throws InterruptedException {
        String demo_key = "etcd_demo_key";
        ReadableDataSource<String, List<FlowRule>> flowRuleEtcdDataSource = new EtcdDataSource<>(demo_key, (value) -> JSON.parseArray(value, FlowRule.class));
        FlowRuleManager.register2Property(flowRuleEtcdDataSource.getProperty());

        KV kvClient = Client.builder()
                .endpoints(endPoints)
                .build().getKVClient();

        final String rule1 = "[\n"
                + "  {\n"
                + "    \"resource\": \"TestResource\",\n"
                + "    \"controlBehavior\": 0,\n"
                + "    \"count\": 5.0,\n"
                + "    \"grade\": 1,\n"
                + "    \"limitApp\": \"default\",\n"
                + "    \"strategy\": 0\n"
                + "  }\n"
                + "]";

        kvClient.put(ByteSequence.from(demo_key.getBytes()), ByteSequence.from(rule1.getBytes()));
        Thread.sleep(1000);

        FlowRule flowRule = FlowRuleManager.getRules().get(0);
        Assert.assertTrue(flowRule.getResource().equals("TestResource"));
        Assert.assertTrue(flowRule.getCount() == 5.0);
        Assert.assertTrue(flowRule.getGrade() == 1);

        final String rule2 = "[\n"
                + "  {\n"
                + "    \"resource\": \"TestResource\",\n"
                + "    \"controlBehavior\": 0,\n"
                + "    \"count\": 6.0,\n"
                + "    \"grade\": 3,\n"
                + "    \"limitApp\": \"default\",\n"
                + "    \"strategy\": 0\n"
                + "  }\n"
                + "]";

        kvClient.put(ByteSequence.from(demo_key.getBytes()), ByteSequence.from(rule2.getBytes()));
        Thread.sleep(1000);

        flowRule = FlowRuleManager.getRules().get(0);
        Assert.assertTrue(flowRule.getResource().equals("TestResource"));
        Assert.assertTrue(flowRule.getCount() == 6.0);
        Assert.assertTrue(flowRule.getGrade() == 3);


    }
}
