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
package com.alibaba.csp.sentinel.demo.apache.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.demo.apache.dubbo.consumer.ConsumerConfiguration;
import com.alibaba.csp.sentinel.demo.apache.dubbo.consumer.FooServiceConsumer;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Please add the following VM arguments:
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.api.port=8721
 * -Dproject.name=dubbo-consumer-demo
 * </pre>
 *
 * @author Zechao zheng
 */
public class FooConsumerExceptionDegradeBootstrap {

    private static final String INTERFACE_RES_KEY = FooService.class.getName();
    private static final String RES_KEY = INTERFACE_RES_KEY + ":sayHello(java.lang.String)";

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        AnnotationConfigApplicationContext consumerContext = new AnnotationConfigApplicationContext();
        consumerContext.register(ConsumerConfiguration.class);
        consumerContext.refresh();

        FooServiceConsumer service = consumerContext.getBean(FooServiceConsumer.class);
        initExceptionFallback(3);
        registryCustomFallback();
        for (int i = 0; i < 10; i++) {
            try {
                String message = service.exceptionTest(true, false);
                System.out.println("Result: " + message);
            } catch (SentinelRpcException ex) {
                System.out.println("Blocked");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        // sleep 3s to skip the time window
        initExceptionFallback(3);
        Thread.sleep(3000);
        for (int i = 0; i < 10; i++) {
            try {
                String message = service.exceptionTest(false, true);
                System.out.println("Result: " + message);
            } catch (SentinelRpcException ex) {
                System.out.println("Blocked");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        initExceptionFallback(3);
        Thread.sleep(3000);

        try {
            // timeout to trigger the fallback
            CompletableFuture<String> completableFuture = RpcContext.getContext().asyncCall(() -> service.exceptionTest(false, true));
            System.out.println("Result: " + completableFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            try {
                CompletableFuture<String> result = RpcContext.getContext().asyncCall(() -> service.exceptionTest(false, true));
                System.out.println("Result: " + result.get());
            } catch (SentinelRpcException ex) {
                System.out.println("Blocked");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void registryCustomFallback() {
        DubboAdapterGlobalConfig.setConsumerFallback(
                (invoker, invocation, ex) -> AsyncRpcResult.newDefaultAsyncResult("fallback", invocation));

    }

    public static void initExceptionFallback(int timewindow) {
        DegradeRule degradeRule = new DegradeRule(INTERFACE_RES_KEY)
                .setCount(0.5)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
                .setTimeWindow(timewindow)
                .setMinRequestAmount(1);
        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));

    }
}
