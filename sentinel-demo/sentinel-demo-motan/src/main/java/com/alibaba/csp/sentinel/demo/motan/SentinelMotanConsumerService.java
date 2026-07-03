/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.motan;

import com.alibaba.csp.sentinel.demo.motan.service.MotanDemoService;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.weibo.api.motan.config.ProtocolConfig;
import com.weibo.api.motan.config.RefererConfig;
import com.weibo.api.motan.config.RegistryConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangxn8
 */
public class SentinelMotanConsumerService {

    private static final String INTERFACE_RES_KEY = MotanDemoService.class.getName();
    private static final String RES_KEY = INTERFACE_RES_KEY + ":hello(java.lang.String)";

    public static void main(String[] args) {
        RefererConfig<MotanDemoService> motanDemoServiceReferer = new RefererConfig<MotanDemoService>();
        // 设置接口及实现类
        motanDemoServiceReferer.setInterface(MotanDemoService.class);
        // 配置服务的group以及版本号
        motanDemoServiceReferer.setGroup("motan-demo-rpc");
        motanDemoServiceReferer.setVersion("1.0");
        motanDemoServiceReferer.setRequestTimeout(100000);

        // 配置注册中心直连调用
        RegistryConfig registry = new RegistryConfig();

        //use direct registry
        registry.setRegProtocol("direct");
        registry.setAddress("127.0.0.1:8002");

        // use ZooKeeper: 2181  or consul:8500 registry
//        registry.setRegProtocol("consul");
//        registry.setAddress("127.0.0.1:8500");
        motanDemoServiceReferer.setRegistry(registry);

        // 配置RPC协议
        ProtocolConfig protocol = new ProtocolConfig();
        protocol.setId("motan");
        protocol.setName("motan");
        motanDemoServiceReferer.setProtocol(protocol);
        motanDemoServiceReferer.setDirectUrl("localhost:8002");  // 注册中心直连调用需添加此配置

        initFlowRule(5, false);

        // 使用服务
        MotanDemoService service = motanDemoServiceReferer.getRef();
        for (int i = 0 ;i< 5000 ;i++) {
            System.out.println(service.hello("motan"));
        }

        System.exit(0);
    }

    private static void initFlowRule(int interfaceFlowLimit, boolean method) {
        FlowRule flowRule = new FlowRule(INTERFACE_RES_KEY)
                .setCount(interfaceFlowLimit)
                .setGrade(RuleConstant.FLOW_GRADE_QPS);
        List<FlowRule> list = new ArrayList<>();
        if (method) {
            FlowRule flowRule1 = new FlowRule(RES_KEY)
                    .setCount(5)
                    .setGrade(RuleConstant.FLOW_GRADE_QPS);
            list.add(flowRule1);
        }
        list.add(flowRule);
        FlowRuleManager.loadRules(list);
    }
}
