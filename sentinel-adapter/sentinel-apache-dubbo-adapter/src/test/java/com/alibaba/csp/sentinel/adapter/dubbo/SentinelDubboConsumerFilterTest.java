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
package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.BaseTest;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DefaultDubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallbackRegistry;
import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.support.RpcUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO;
import static org.apache.dubbo.rpc.Constants.ASYNC_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author cdfive
 */
public class SentinelDubboConsumerFilterTest extends BaseTest {

    private SentinelDubboConsumerFilter filter = new SentinelDubboConsumerFilter();


    @Before
    public void setUp() {
        cleanUpAll();
        initFallback();
        constructInvokerAndInvocation();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
        DubboFallbackRegistry.setConsumerFallback(new DefaultDubboFallback());
    }

    public void initFlowRule(String resource) {
        FlowRule flowRule = new FlowRule(resource);
        flowRule.setCount(1);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        List<FlowRule> flowRules = new ArrayList<>();
        flowRules.add(flowRule);
        FlowRuleManager.loadRules(flowRules);
    }

    public void initDegradeRule(String resource) {
        DegradeRule degradeRule = new DegradeRule(resource)
                .setCount(0.5)
                .setGrade(DEGRADE_GRADE_EXCEPTION_RATIO);
        List<DegradeRule> degradeRules = new ArrayList<>();
        degradeRules.add(degradeRule);
        degradeRule.setTimeWindow(1);
        DegradeRuleManager.loadRules(degradeRules);
    }


    public void initFallback() {
        DubboFallbackRegistry.setConsumerFallback(new DubboFallback() {
            @Override
            public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
                boolean async = RpcUtils.isAsync(invoker.getUrl(), invocation);
                Result fallbackResult = null;
                fallbackResult = AsyncRpcResult.newDefaultAsyncResult("fallback", invocation);
                return fallbackResult;
            }
        });
    }

    @Test
    public void testInterfaceLevelFollowControlAsync() throws InterruptedException {
        when(invocation.getAttachment(ASYNC_KEY)).thenReturn(Boolean.TRUE.toString());
        initFlowRule(invoker.getUrl().getColonSeparatedKey());
        Result result1 = responseBack(requestGo(false, invocation));
        assertEquals("normal", result1.getValue());
        // should fallback because the qps > 1
        Result result2 = responseBack(requestGo(false, invocation));
        assertEquals("fallback", result2.getValue());
        // sleeping 1000 ms to reset qps
        Thread.sleep(1000);
        Result result3 = responseBack(requestGo(false, invocation));
        assertEquals("normal", result3.getValue());

        verifyInvocationStructureForCallFinish();
    }

    @Test
    public void testDegradeAsync() throws InterruptedException {
        when(invocation.getAttachment(ASYNC_KEY)).thenReturn(Boolean.TRUE.toString());

        initDegradeRule(invoker.getUrl().getColonSeparatedKey());
        Result result = requestGo(false, invocation);
        verifyInvocationStructureForAsyncCall(invoker, invocation);
        responseBack(result);
        assertEquals("normal", result.getValue());
        // inc the clusterNode's exception to trigger the fallback
        for (int i = 0; i < 5; i++) {
            responseBack(requestGo(true, invocation));
            verifyInvocationStructureForCallFinish();
        }
        Result result2 = responseBack(requestGo(false, invocation));
        assertEquals("fallback", result2.getValue());
        // sleeping 1000 ms to reset exception
        Thread.sleep(1000);

        Result result3 = responseBack(requestGo(false, invocation));
        assertEquals("normal", result3.getValue());

        Context context = ContextUtil.getContext();
        assertNull(context);
    }

    @Test
    public void testDegradeSync() throws InterruptedException {

        initDegradeRule(invoker.getUrl().getColonSeparatedKey());
        Result result = requestGo(false, invocation);
        verifyInvocationStructure(invoker, invocation);
        responseBack(result);
        assertEquals("normal", result.getValue());
        // inc the clusterNode's exception to trigger the fallback
        for (int i = 0; i < 5; i++) {
            responseBack(requestGo(true, invocation));
            verifyInvocationStructureForCallFinish();
        }
        Result result2 = responseBack(requestGo(false, invocation));
        assertEquals("fallback", result2.getValue());
        // sleeping 1000 ms to reset exception
        Thread.sleep(1000);

        Result result3 = responseBack(requestGo(false, invocation));
        assertEquals("normal", result3.getValue());

        Context context = ContextUtil.getContext();
        assertNull(context);
    }


    @Test
    public void testMethodFlowControlAsync() {
        when(invocation.getAttachment(ASYNC_KEY)).thenReturn(Boolean.TRUE.toString());
        initFlowRule(DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboConsumerPrefix()));
        responseBack(requestGo(false, invocation));

        responseBack(requestGo(false, invocation));

        Invocation invocation2 = mock(Invocation.class);
        Method method = DemoService.class.getMethods()[1];
        when(invocation2.getMethodName()).thenReturn(method.getName());
        when(invocation2.getParameterTypes()).thenReturn(method.getParameterTypes());
        Result result2 = responseBack(requestGo(false, invocation2));
        verifyInvocationStructureForCallFinish();
        assertEquals("normal", result2.getValue());

        // the method of invocation should be blocked
        Result fallback = requestGo(false, invocation);
        assertNotNull(RpcContext.getContext().get(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY));
        assertNull(RpcContext.getContext().get(DubboUtils.DUBBO_METHOD_ENTRY_KEY));
        responseBack(fallback);
        assertEquals("fallback", fallback.getValue());
        verifyInvocationStructureForCallFinish();


    }

    public Result requestGo(boolean exception, Invocation currentInvocation) {
        AsyncRpcResult result = null;

        if (exception) {
            result = AsyncRpcResult.newDefaultAsyncResult(new Exception("error"), currentInvocation);
        } else {
            result = AsyncRpcResult.newDefaultAsyncResult("normal", currentInvocation);
        }
        when(invoker.invoke(currentInvocation)).thenReturn(result);
        return filter.invoke(invoker, currentInvocation);
    }

    public Result responseBack(Result result) {
        filter.listener().onMessage(result, invoker, invocation);
        return result;
    }


    @Test
    public void testInvokeAsync() throws InterruptedException {

        when(invocation.getAttachment(ASYNC_KEY)).thenReturn(Boolean.TRUE.toString());

        final Result result = mock(Result.class);
        when(result.hasException()).thenReturn(false);
        when(invoker.invoke(invocation)).thenAnswer(invocationOnMock -> {
            verifyInvocationStructureForAsyncCall(invoker, invocation);
            return result;
        });

        filter.invoke(invoker, invocation);
        verify(invoker).invoke(invocation);

        Context context = ContextUtil.getContext();
        assertNotNull(context);
    }

    @Test
    public void testInvokeSync() {

        final Result result = mock(Result.class);
        when(result.hasException()).thenReturn(false);
        when(result.getException()).thenReturn(new Exception());
        when(invoker.invoke(invocation)).thenAnswer(invocationOnMock -> {
            verifyInvocationStructure(invoker, invocation);
            return result;
        });

        filter.invoke(invoker, invocation);
        verify(invoker).invoke(invocation);

        filter.listener().onMessage(result, invoker, invocation);
        Context context = ContextUtil.getContext();
        assertNull(context);
    }

    /**
     * Simply verify invocation structure in memory:
     * EntranceNode(defaultContextName)
     * --InterfaceNode(interfaceName)
     * ----MethodNode(resourceName)
     */
    private void verifyInvocationStructure(Invoker invoker, Invocation invocation) {
        Context context = ContextUtil.getContext();
        assertNotNull(context);
        // As not call ContextUtil.enter(resourceName, application) in SentinelDubboConsumerFilter, use default context
        // In actual project, a consumer is usually also a provider, the context will be created by SentinelDubboProviderFilter
        // If consumer is on the top of Dubbo RPC invocation chain, use default context
        String resourceName = DubboUtils.getResourceName(invoker, invocation, true);
        assertEquals(com.alibaba.csp.sentinel.Constants.CONTEXT_DEFAULT_NAME, context.getName());
        assertEquals("", context.getOrigin());

        DefaultNode entranceNode = context.getEntranceNode();
        ResourceWrapper entranceResource = entranceNode.getId();

        assertEquals(com.alibaba.csp.sentinel.Constants.CONTEXT_DEFAULT_NAME, entranceResource.getName());
        assertSame(EntryType.IN, entranceResource.getEntryType());

        // As SphU.entry(interfaceName, EntryType.OUT);
        Set<Node> childList = entranceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode interfaceNode = getNode(invoker.getUrl().getColonSeparatedKey(), entranceNode);
        ResourceWrapper interfaceResource = interfaceNode.getId();

        assertEquals(invoker.getUrl().getColonSeparatedKey(), interfaceResource.getName());
        assertSame(EntryType.OUT, interfaceResource.getEntryType());

        // As SphU.entry(resourceName, EntryType.OUT);
        childList = interfaceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode methodNode = getNode(resourceName, entranceNode);
        ResourceWrapper methodResource = methodNode.getId();
        assertEquals(resourceName, methodResource.getName());
        assertSame(EntryType.OUT, methodResource.getEntryType());

        // Verify curEntry
        Entry curEntry = context.getCurEntry();
        assertSame(methodNode, curEntry.getCurNode());
        assertSame(interfaceNode, curEntry.getLastNode());
        assertNull(curEntry.getOriginNode());// As context origin is not "", no originNode should be created in curEntry

        // Verify clusterNode
        ClusterNode methodClusterNode = methodNode.getClusterNode();
        ClusterNode interfaceClusterNode = interfaceNode.getClusterNode();
        assertNotSame(methodClusterNode, interfaceClusterNode);// Different resource->Different ProcessorSlot->Different ClusterNode

        // As context origin is "", the StatisticNode should not be created in originCountMap of ClusterNode
        Map<String, StatisticNode> methodOriginCountMap = methodClusterNode.getOriginCountMap();
        assertEquals(0, methodOriginCountMap.size());

        Map<String, StatisticNode> interfaceOriginCountMap = interfaceClusterNode.getOriginCountMap();
        assertEquals(0, interfaceOriginCountMap.size());
    }

    private void verifyInvocationStructureForAsyncCall(Invoker invoker, Invocation invocation) {
        Context context = ContextUtil.getContext();
        assertNotNull(context);

        // As not call ContextUtil.enter(resourceName, application) in SentinelDubboConsumerFilter, use default context
        // In actual project, a consumer is usually also a provider, the context will be created by SentinelDubboProviderFilter
        // If consumer is on the top of Dubbo RPC invocation chain, use default context
        String resourceName = DubboUtils.getResourceName(invoker, invocation, true);
        assertEquals(com.alibaba.csp.sentinel.Constants.CONTEXT_DEFAULT_NAME, context.getName());
        assertEquals("", context.getOrigin());

        DefaultNode entranceNode = context.getEntranceNode();
        ResourceWrapper entranceResource = entranceNode.getId();
        assertEquals(com.alibaba.csp.sentinel.Constants.CONTEXT_DEFAULT_NAME, entranceResource.getName());
        assertSame(EntryType.IN, entranceResource.getEntryType());

        // As SphU.entry(interfaceName, EntryType.OUT);
        Set<Node> childList = entranceNode.getChildList();
        assertEquals(2, childList.size());
        DefaultNode interfaceNode = getNode(invoker.getUrl().getColonSeparatedKey(), entranceNode);
        ResourceWrapper interfaceResource = interfaceNode.getId();
        assertEquals(invoker.getUrl().getColonSeparatedKey(), interfaceResource.getName());
        assertSame(EntryType.OUT, interfaceResource.getEntryType());

        // As SphU.entry(resourceName, EntryType.OUT);
        childList = interfaceNode.getChildList();
        assertEquals(0, childList.size());
        DefaultNode methodNode = getNode(resourceName, entranceNode);
        ResourceWrapper methodResource = methodNode.getId();
        assertEquals(resourceName, methodResource.getName());
        assertSame(EntryType.OUT, methodResource.getEntryType());

        // Verify curEntry
        // nothing will bind to local context when use the AsyncEntry
        Entry curEntry = context.getCurEntry();
        assertNull(curEntry);

        // Verify clusterNode
        ClusterNode methodClusterNode = methodNode.getClusterNode();
        ClusterNode interfaceClusterNode = interfaceNode.getClusterNode();
        assertNotSame(methodClusterNode, interfaceClusterNode);// Different resource->Different ProcessorSlot->Different ClusterNode

        // As context origin is "", the StatisticNode should not be created in originCountMap of ClusterNode
        Map<String, StatisticNode> methodOriginCountMap = methodClusterNode.getOriginCountMap();
        assertEquals(0, methodOriginCountMap.size());

        Map<String, StatisticNode> interfaceOriginCountMap = interfaceClusterNode.getOriginCountMap();
        assertEquals(0, interfaceOriginCountMap.size());
    }


    private void verifyInvocationStructureForCallFinish() {
        Context context = ContextUtil.getContext();
        assertNull(context);
        Entry interfaceEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_INTERFACE_ENTRY_KEY);
        Entry methodEntry = (Entry) RpcContext.getContext().get(DubboUtils.DUBBO_METHOD_ENTRY_KEY);
        assertNull(interfaceEntry);
        assertNull(methodEntry);
    }


    public DefaultNode getNode(String resourceName, DefaultNode root) {

        Queue<DefaultNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            DefaultNode temp = queue.poll();
            if (temp.getId().getName().equals(resourceName)) {
                return temp;
            }
            for (Node node : temp.getChildList()) {
                queue.offer((DefaultNode) node);
            }
        }
        return null;
    }

}
