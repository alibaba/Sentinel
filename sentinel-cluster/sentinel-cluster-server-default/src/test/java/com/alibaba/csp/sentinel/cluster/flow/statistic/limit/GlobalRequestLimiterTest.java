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
package com.alibaba.csp.sentinel.cluster.flow.statistic.limit;

import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import org.junit.Assert;
import org.junit.Test;

public class GlobalRequestLimiterTest {
    @Test
    public void testPass() throws InterruptedException {
        ClusterServerConfigManager.setMaxAllowedQps(3);
        GlobalRequestLimiter.initIfAbsent("user");
        Assert.assertNotNull(GlobalRequestLimiter.getRequestLimiter("user"));
        Assert.assertEquals(3, GlobalRequestLimiter.getMaxAllowedQps("user"), 0.01);
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertFalse(GlobalRequestLimiter.tryPass("user"));
        Assert.assertEquals(3, GlobalRequestLimiter.getCurrentQps("user"), 0.01);

        // wait a second to refresh the window
        Thread.sleep(1000);
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertEquals(2, GlobalRequestLimiter.getCurrentQps("user"), 0.01);
    }
    @Test
    public void testChangeMaxAllowedQps(){
        ClusterServerConfigManager.setMaxAllowedQps(4000);
        GlobalRequestLimiter.initIfAbsent("foo");
        Assert.assertEquals(4000, GlobalRequestLimiter.getMaxAllowedQps("foo"), 0.01);
        GlobalRequestLimiter.applyMaxQpsChange(10);
        Assert.assertEquals(10, GlobalRequestLimiter.getMaxAllowedQps("foo"), 0.01);
    }
}
