package com.alibaba.csp.sentinel.adapter.dubbo;

import com.alibaba.csp.sentinel.BaseTest;
import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author cdfive
 */
public class SentinelDubboProviderFilterTest extends BaseTest {

    private SentinelDubboProviderFilter filter = new SentinelDubboProviderFilter();

    @Before
    public void setUp() {
        cleanUpAll();
    }

    @After
    public void cleanUp() {
        cleanUpAll();
    }

    @Test
    public void testInvoke() {
        final String originApplication = "consumerA";

        final Invoker invoker = mock(Invoker.class);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        final Invocation invocation = mock(Invocation.class);
        Method method = DemoService.class.getMethods()[0];
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());
        when(invocation.getAttachment(DubboUtils.DUBBO_APPLICATION_KEY, "")).thenReturn(originApplication);

        final Result result = mock(Result.class);
        when(result.hasException()).thenReturn(false);
        when(invoker.invoke(invocation)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                verifyInvocationStructure(originApplication, invoker, invocation);
                return result;
            }
        });

        filter.invoke(invoker, invocation);
        verify(invoker).invoke(invocation);

        Context context = ContextUtil.getContext();
        assertNull(context);
    }

    /**
     * Simply verify invocation structure in memory:
     * EntranceNode(resourceName)
     * --InterfaceNode(interfaceName)
     * ----MethodNode(resourceName)
     */
    private void verifyInvocationStructure(String originApplication, Invoker invoker, Invocation invocation) {
        Context context = ContextUtil.getContext();
        assertNotNull(context);

        // As ContextUtil.enter(resourceName, application) in SentinelDubboProviderFilter
        String resourceName = filter.getResourceName(invoker, invocation);
        assertEquals(resourceName, context.getName());
        assertEquals(originApplication, context.getOrigin());

        DefaultNode entranceNode = context.getEntranceNode();
        ResourceWrapper entranceResource = entranceNode.getId();
        assertEquals(resourceName, entranceResource.getName());
        assertSame(EntryType.IN, entranceResource.getType());

        // As SphU.entry(interfaceName, EntryType.IN);
        Set<Node> childList = entranceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode interfaceNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper interfaceResource = interfaceNode.getId();
        assertEquals(DemoService.class.getName(), interfaceResource.getName());
        assertSame(EntryType.IN, interfaceResource.getType());

        // As SphU.entry(resourceName, EntryType.IN, 1, invocation.getArguments());
        childList = interfaceNode.getChildList();
        assertEquals(1, childList.size());
        DefaultNode methodNode = (DefaultNode) childList.iterator().next();
        ResourceWrapper methodResource = methodNode.getId();
        assertEquals(resourceName, methodResource.getName());
        assertSame(EntryType.IN, methodResource.getType());

        // Verify curEntry
        Entry curEntry = context.getCurEntry();
        assertSame(methodNode, curEntry.getCurNode());
        assertSame(interfaceNode, curEntry.getLastNode());
        assertNotNull(curEntry.getOriginNode());// As context origin is not "", originNode should be created

        // Verify clusterNode
        ClusterNode methodClusterNode = methodNode.getClusterNode();
        ClusterNode interfaceClusterNode = interfaceNode.getClusterNode();
        assertNotSame(methodClusterNode, interfaceClusterNode);// Different resource->Different ProcessorSlot->Different ClusterNode

        // As context origin is not "", the StatisticNode should be created in originCountMap of ClusterNode
        Map<String, StatisticNode> methodOriginCountMap = methodClusterNode.getOriginCountMap();
        assertEquals(1, methodOriginCountMap.size());
        assertTrue(methodOriginCountMap.containsKey(originApplication));

        Map<String, StatisticNode> interfaceOriginCountMap = interfaceClusterNode.getOriginCountMap();
        assertEquals(1, interfaceOriginCountMap.size());
        assertTrue(interfaceOriginCountMap.containsKey(originApplication));
    }
}
