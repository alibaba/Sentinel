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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * This class is used to record other exception except block exception.
 *
 * @author jialiang.linjl
 */
public final class Tracer {

    public static void trace(Throwable e) {
        trace(e, 1);
    }

    public static void trace(Throwable e, int count) {
        if (e instanceof BlockException) {
            return;
        }

        Context context = ContextUtil.getContext();
        if (context == null) {
            return;
        }

        DefaultNode curNode = (DefaultNode)context.getCurNode();
        if (curNode == null) {
            return;
        }

        // clusterNode can be null when Constants.ON is false.
        ClusterNode clusterNode = curNode.getClusterNode();
        if (clusterNode == null) {
            return;
        }
        clusterNode.trace(e, count);
    }

}
