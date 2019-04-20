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
package com.alibaba.csp.sentinel.demo.dubbo.demo2;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.demo.dubbo.consumer.ConsumerConfiguration;
import com.alibaba.csp.sentinel.demo.dubbo.consumer.FooServiceConsumer;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Please add the following VM arguments:
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.api.port=8721
 * -Dproject.name=dubbo-consumer-demo
 * </pre>
 *
 * @author Eric Zhao
 */
public class FooConsumerBootstrap {

    private static final String RES_KEY = "com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String)";
    private static final String INTERFACE_RES_KEY = "com.alibaba.csp.sentinel.demo.dubbo.FooService";

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private static final ExecutorService pool = Executors.newFixedThreadPool(10,
        new NamedThreadFactory("dubbo-consumer-pool"));

    public static void main(String[] args) {
        initFlowRule();

        AnnotationConfigApplicationContext consumerContext = new AnnotationConfigApplicationContext();
        consumerContext.register(ConsumerConfiguration.class);
        consumerContext.refresh();

        FooServiceConsumer service = consumerContext.getBean(FooServiceConsumer.class);
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                try {
                    String message = service.sayHello("Eric");
                    System.out.println("Success: " + message);
                } catch (SentinelRpcException ex) {
                    System.out.println("Blocked");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            pool.submit(() -> System.out.println("Another: " + service.doAnother()));
        }
    }

    private static void initFlowRule() {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(RES_KEY);
        flowRule.setCount(5);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }

    private static void registerFallback() {
        // Register fallback handler for consumer.
        // If you only want to handle degrading, you need to
        // check the type of BlockException.
        DubboFallbackRegistry.setConsumerFallback((a, b, ex) ->
            new RpcResult("Error: " + ex.getClass().getTypeName()));
    }
}
