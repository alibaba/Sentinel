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
package com.alibaba.csp.sentinel.adapter.sofa.rpc;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alipay.sofa.rpc.common.RpcConstants;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link SentinelSofaRpcProviderFilter}.
 *
 * @author cdfive
 */
public class SentinelSofaRpcProviderFilterTest extends BaseTest {

    @Before
    public void setUp() {
        cleanUpAll();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
    }

    @Test
    public void testInvokeSentinelWorks() {
        SentinelSofaRpcProviderFilter filter = new SentinelSofaRpcProviderFilter();

        final String applicationName = "demo-provider";
        final String interfaceResourceName = "com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService";
        final String methodResourceName = "com.alibaba.csp.sentinel.adapter.sofa.rpc.service.DemoService#sayHello(java.lang.String,int)";

        SofaRequest request = mock(SofaRequest.class);
        when(request.getRequestProp("app")).thenReturn(applicationName);
        when(request.getInvokeType()).thenReturn(RpcConstants.INVOKER_TYPE_SYNC);
        when(request.getInterfaceName()).thenReturn(interfaceResourceName);
        when(request.getMethodName()).thenReturn("sayHello");
        when(request.getMethodArgSigs()).thenReturn(new String[]{"java.lang.String", "int"});
        when(request.getMethodArgs()).thenReturn(new Object[]{"Sentinel", 2020});

        FilterInvoker filterInvoker = mock(FilterInvoker.class);
        when(filterInvoker.invoke(request)).thenAnswer(new Answer<SofaResponse>() {
            @Override
            public SofaResponse answer(InvocationOnMock invocationOnMock) throws Throwable {
                verifyInvocationStructure(applicationName, interfaceResourceName, methodResourceName);
                SofaResponse response = new SofaResponse();
                response.setAppResponse("Hello Sentinel 2020");
                return response;
            }
        });

        // Before invoke
        assertNull(ContextUtil.getContext());

        // Do invoke
        SofaResponse response = filter.invoke(filterInvoker, request);
        assertEquals("Hello Sentinel 2020", response.getAppResponse());
        verify(filterInvoker).invoke(request);

        // After invoke, make sure exit context
        assertNull(ContextUtil.getContext());
    }

    /**
     * Verify Sentinel invocation structure in memory:
     * EntranceNode(methodResourceName)
     * --InterfaceNode(interfaceResourceName)
     * ----MethodNode(methodResourceName)
     */
    private void verifyInvocationStructure(String applicationName, String interfaceResourceName, String methodResourceName) {
        Context context = ContextUtil.getContext();
        assertNotNull(context);

        assertEquals(methodResourceName, context.getName());
        assertEquals(applicationName, context.getOrigin());

        DefaultNode entranceNode = context.getEntranceNode();
        ResourceWrapper entranceResource = entranceNode.getId();
        assertEquals(methodResourceName, entranceResource.getName());
        assertSame(EntryType.IN, entranceResource.getEntryType());

        // As SphU.entry(interfaceResourceName, EntryType.IN);
        Set<Node> childList = entranceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode interfaceNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper interfaceResource = interfaceNode.getId();
        assertEquals(interfaceResourceName, interfaceResource.getName());
        assertSame(EntryType.IN, interfaceResource.getEntryType());

        // As SphU.entry(methodResourceName, EntryType.IN, 1, methodArguments);
        childList = interfaceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode methodNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper methodResource = methodNode.getId();
        assertEquals(methodResourceName, methodResource.getName());
        assertSame(EntryType.IN, methodResource.getEntryType());

        // Verify curEntry
        Entry curEntry = context.getCurEntry();
        assertSame(methodNode, curEntry.getCurNode());
        assertSame(interfaceNode, curEntry.getLastNode());
        // As context origin is not "", originNode should be created
        assertNotNull(curEntry.getOriginNode());

        // Verify clusterNode
        ClusterNode methodClusterNode = methodNode.getClusterNode();
        ClusterNode interfaceClusterNode = interfaceNode.getClusterNode();
        // Different resource->Different ProcessorSlot->Different ClusterNode
        assertNotSame(methodClusterNode, interfaceClusterNode);

        // As context origin is not "", the StatisticNode should be created in originCountMap of ClusterNode
        Map<String, StatisticNode> methodOriginCountMap = methodClusterNode.getOriginCountMap();
        assertEquals(1, methodOriginCountMap.size());
        assertTrue(methodOriginCountMap.containsKey(applicationName));
        Map<String, StatisticNode> interfaceOriginCountMap = interfaceClusterNode.getOriginCountMap();
        assertEquals(1, interfaceOriginCountMap.size());
        assertTrue(interfaceOriginCountMap.containsKey(applicationName));
    }
}
