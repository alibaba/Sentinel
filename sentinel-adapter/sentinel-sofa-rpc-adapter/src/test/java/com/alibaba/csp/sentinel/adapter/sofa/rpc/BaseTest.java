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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.CtSph;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import java.lang.reflect.Method;

/**
 * Base test class, provide common methods for sub test class.
 *
 * Note: Only for test. DO NOT USE IN PRODUCTION!
 *
 * @author cdfive
 */
public class BaseTest {

    /**
     * Clean up resources.
     */
    protected static void cleanUpAll() {
        Context context = ContextUtil.getContext();
        if (context != null) {
            context.setCurEntry(null);
            ContextUtil.exit();
        }

        Constants.ROOT.removeChildList();

        ClusterBuilderSlot.getClusterNodeMap().clear();

        // Clear chainMap in CtSph
        try {
            Method resetChainMapMethod = CtSph.class.getDeclaredMethod("resetChainMap");
            resetChainMapMethod.setAccessible(true);
            resetChainMapMethod.invoke(null);
        } catch (Exception e) {
            // Empty
        }
    }
}