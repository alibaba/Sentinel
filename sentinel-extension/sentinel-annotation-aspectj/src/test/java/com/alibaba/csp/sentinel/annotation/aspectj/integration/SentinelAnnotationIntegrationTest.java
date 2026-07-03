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
package com.alibaba.csp.sentinel.annotation.aspectj.integration;

import com.alibaba.csp.sentinel.annotation.aspectj.integration.config.AopTestConfig;
import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.BarService;
import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.FooService;
import com.alibaba.csp.sentinel.annotation.aspectj.integration.service.FooUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.MethodUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for Sentinel annotation AspectJ extension.
 *
 * @author Eric Zhao
 * @author zhaoyuguang
 */
@ContextConfiguration(classes = {SentinelAnnotationIntegrationTest.class, AopTestConfig.class})
public class SentinelAnnotationIntegrationTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private FooService fooService;
    @Autowired
    private BarService barService;

    @Test
    public void testProxySuccessful() {
        assertThat(AopUtils.isAopProxy(fooService)).isTrue();
        assertThat(AopUtils.isCglibProxy(fooService)).isTrue();
    }

    @Test
    public void testForeignBlockHandlerClass() throws Exception {
        assertThat(fooService.random()).isNotEqualTo(FooUtil.BLOCK_FLAG);
        String resourceName = MethodUtil.resolveMethodName(FooService.class.getDeclaredMethod("random"));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        FlowRuleManager.loadRules(Collections.singletonList(
                new FlowRule(resourceName).setCount(0)
        ));
        assertThat(fooService.random()).isEqualTo(FooUtil.BLOCK_FLAG);
        assertThat(cn.blockQps()).isPositive();
    }

    @Test(expected = UndeclaredThrowableException.class)
    public void testBlockHandlerNotFound() {
        assertThat(fooService.baz("Sentinel")).isEqualTo("cheers, Sentinel");
        String resourceName = "apiBaz";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        FlowRuleManager.loadRules(Collections.singletonList(
                new FlowRule(resourceName).setCount(0)
        ));
        fooService.baz("Sentinel");
    }

    @Test
    public void testAnnotationExceptionsToIgnore() {
        assertThat(fooService.baz("Sentinel")).isEqualTo("cheers, Sentinel");
        String resourceName = "apiBaz";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        try {
            fooService.baz("fail");
            fail("should not reach here");
        } catch (IllegalMonitorStateException ex) {
            assertThat(cn.exceptionQps()).isZero();
        }
    }

    @Test
    public void testFallbackWithNoParams() throws Exception {
        assertThat(fooService.fooWithFallback(1)).isEqualTo("Hello for 1");
        String resourceName = "apiFooWithFallback";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        // Fallback should be ignored for this.
        try {
            fooService.fooWithFallback(5758);
            fail("should not reach here");
        } catch (IllegalAccessException e) {
            assertThat(cn.exceptionQps()).isZero();
        }

        // Fallback should take effect.
        assertThat(fooService.fooWithFallback(5763)).isEqualTo("eee...");
        assertThat(cn.exceptionQps()).isPositive();
        assertThat(cn.blockQps()).isZero();

        FlowRuleManager.loadRules(Collections.singletonList(
            new FlowRule(resourceName).setCount(0)
        ));
        // Fallback should not take effect for BlockException, as blockHandler is configured.
        assertThat(fooService.fooWithFallback(2221)).isEqualTo("Oops, 2221");
        assertThat(cn.blockQps()).isPositive();
    }

    @Test
    public void testDefaultFallbackWithSingleParam() {
        assertThat(fooService.anotherFoo(1)).isEqualTo("Hello for 1");
        String resourceName = "apiAnotherFooWithDefaultFallback";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        // Default fallback should take effect.
        assertThat(fooService.anotherFoo(5758)).isEqualTo(FooUtil.FALLBACK_DEFAULT_RESULT);
        assertThat(cn.exceptionQps()).isPositive();
        assertThat(cn.blockQps()).isZero();

        FlowRuleManager.loadRules(Collections.singletonList(
            new FlowRule(resourceName).setCount(0)
        ));
        // Default fallback should also take effect for BlockException.
        assertThat(fooService.anotherFoo(5758)).isEqualTo(FooUtil.FALLBACK_DEFAULT_RESULT);
        assertThat(cn.blockQps()).isPositive();
    }

    @Test
    public void testNormalBlockHandlerAndFallback() throws Exception {
        assertThat(fooService.foo(1)).isEqualTo("Hello for 1");
        String resourceName = "apiFoo";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        // Test for biz exception.
        try {
            fooService.foo(5758);
            fail("should not reach here");
        } catch (Exception ex) {
            // Should not be traced.
            assertThat(cn.exceptionQps()).isZero();
        }

        try {
            fooService.foo(5763);
            fail("should not reach here");
        } catch (Exception ex) {
            assertThat(cn.exceptionQps()).isPositive();
        }

        // Test for blockHandler
        FlowRuleManager.loadRules(Collections.singletonList(
                new FlowRule(resourceName).setCount(0)
        ));
        assertThat(fooService.foo(1121)).isEqualTo("Oops, 1121");
        assertThat(cn.blockQps()).isPositive();
    }

    @Test
    public void testClassLevelDefaultFallbackWithSingleParam() {
        assertThat(barService.anotherBar(1)).isEqualTo("Hello for 1");
        String resourceName = "apiAnotherBarWithDefaultFallback";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertThat(cn).isNotNull();
        assertThat(cn.passQps()).isPositive();

        assertThat(barService.doSomething(1)).isEqualTo("do something");
        String resourceName1 = "com.alibaba.csp.sentinel.annotation.aspectj.integration.service.BarService:doSomething(int)";
        ClusterNode cn1 = ClusterBuilderSlot.getClusterNode(resourceName1);
        assertThat(cn1).isNotNull();
        assertThat(cn1.passQps()).isPositive();

        assertThat(barService.anotherBar(5758)).isEqualTo("eee...");
        assertThat(cn.exceptionQps()).isPositive();
        assertThat(cn.blockQps()).isZero();

        assertThat(barService.doSomething(5758)).isEqualTo("GlobalFallback:doFallback");
        assertThat(cn1.exceptionQps()).isPositive();
        assertThat(cn1.blockQps()).isZero();
    }

    @Test
    public void testFallBackPrivateMethod() throws Exception {
        String resourceName = "apiFooWithFallback";
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);

        try {
            fooService.fooWithPrivateFallback(5758);
            fail("should not reach here");
        } catch (Exception ex) {
            // Should not be traced.
            assertThat(cn.exceptionQps()).isZero();
        }

        assertThat(fooService.fooWithPrivateFallback(5763)).isEqualTo("EEE...");

        // Test for blockHandler
        FlowRuleManager.loadRules(Collections.singletonList(
                new FlowRule(resourceName).setCount(0)
        ));
        assertThat(fooService.fooWithPrivateFallback(2221)).isEqualTo("Oops, 2221");
    }

    @Before
    public void setUp() throws Exception {
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        ClusterBuilderSlot.resetClusterNodes();
    }

    @After
    public void tearDown() throws Exception {
        FlowRuleManager.loadRules(new ArrayList<FlowRule>());
        ClusterBuilderSlot.resetClusterNodes();
    }
}
