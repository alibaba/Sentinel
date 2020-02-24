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
package com.alibaba.csp.sentinel;

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.provider.DemoService;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base test class, provide common methods for subClass
 * The package is same as CtSph, to call CtSph.resetChainMap() method for test
 * <p>
 * Note: Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author cdfive
 */
public class BaseTest {


    protected Invoker invoker;
    protected Invocation invocation;

    public void constructInvokerAndInvocation() {
        invoker = mock(Invoker.class);
        URL url = URL.valueOf("dubbo://127.0.0.1:2181")
                .addParameter(CommonConstants.VERSION_KEY, "1.0.0")
                .addParameter(CommonConstants.GROUP_KEY, "grp1")
                .addParameter(CommonConstants.INTERFACE_KEY, DemoService.class.getName());
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.getInterface()).thenReturn(DemoService.class);

        invocation = mock(Invocation.class);
        Method method = DemoService.class.getMethods()[0];
        when(invocation.getMethodName()).thenReturn(method.getName());
        when(invocation.getParameterTypes()).thenReturn(method.getParameterTypes());

    }

    /**
     * Clean up resources for context, clusterNodeMap, processorSlotChainMap
     */
    protected static void cleanUpAll() {
        try {
            RpcContext.removeContext();
            ClusterBuilderSlot.getClusterNodeMap().clear();
            CtSph.resetChainMap();
            Method method = ContextUtil.class.getDeclaredMethod("resetContextMap");
            method.setAccessible(true);
            method.invoke(null, null);
            ContextUtil.exit();
            SentinelConfig.setConfig(DubboConfig.DUBBO_INTERFACE_GROUP_VERSION_ENABLED, "true");
            FlowRuleManager.loadRules(new ArrayList<>());
            DegradeRuleManager.loadRules(new ArrayList<>());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}