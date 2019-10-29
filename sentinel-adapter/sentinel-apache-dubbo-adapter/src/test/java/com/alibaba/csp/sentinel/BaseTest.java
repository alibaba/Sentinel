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
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.apache.dubbo.rpc.RpcContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Base test class, provide common methods for subClass
 * The package is same as CtSph, to call CtSph.resetChainMap() method for test
 *
 * Note: Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author cdfive
 */
public class BaseTest {

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
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}