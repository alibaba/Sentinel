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
package com.alibaba.csp.sentinel.demo.cluster;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * <p>Run this demo with the following args: -Dproject.name=appA</p>
 * <p>You need a token server running already.</p>
 *
 * @author Eric Zhao
 */
public class ClusterClientDemo {

    public static void main(String[] args) {
        InitExecutor.doInit();

        // Manually schedule the cluster mode to client.
        // In common, we need a scheduling system to modify the cluster mode automatically.
        // Command HTTP API: http://<ip>:<port>/setClusterMode?mode=<xxx>
        ClusterStateManager.setToClient();

        String resourceName = "cluster-demo-entry";

        // Assume we have a cluster flow rule for `demo-resource`: QPS = 5 in AVG_LOCAL mode.
        for (int i = 0; i < 10; i++) {
            tryEntry(resourceName);
        }
    }

    private static void tryEntry(String res) {
        Entry entry = null;
        try {
            entry = SphU.entry(res, EntryType.IN, 1, "abc", "def");
            System.out.println("Passed");
        } catch (BlockException ex) {
            ex.printStackTrace();
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }
}
