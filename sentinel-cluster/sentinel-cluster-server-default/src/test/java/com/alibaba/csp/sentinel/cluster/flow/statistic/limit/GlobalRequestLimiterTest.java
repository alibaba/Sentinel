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
import com.alibaba.csp.sentinel.cluster.test.AbstractTimeBasedTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GlobalRequestLimiterTest extends AbstractTimeBasedTest {
    @Before
    public void preTest() {
        ClusterServerConfigManager.setMaxAllowedQps(3);
    }

    @Test
    public void testPass() throws InterruptedException {
        setCurrentMillis(System.currentTimeMillis());
        GlobalRequestLimiter.initIfAbsent("user");
        Assert.assertNotNull(GlobalRequestLimiter.getRequestLimiter("user"));
        Assert.assertEquals(3, GlobalRequestLimiter.getMaxAllowedQps("user"), 0.01);
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertFalse(GlobalRequestLimiter.tryPass("user"));
        Assert.assertEquals(3, GlobalRequestLimiter.getCurrentQps("user"), 0.01);

        // wait a second to refresh the window
        sleep(1000);
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertTrue(GlobalRequestLimiter.tryPass("user"));
        Assert.assertEquals(2, GlobalRequestLimiter.getCurrentQps("user"), 0.01);
    }

    @Test
    public void testChangeMaxAllowedQps() {
        GlobalRequestLimiter.initIfAbsent("foo");
        Assert.assertEquals(3, GlobalRequestLimiter.getMaxAllowedQps("foo"), 0.01);
        GlobalRequestLimiter.applyMaxQpsChange(10);
        Assert.assertEquals(10, GlobalRequestLimiter.getMaxAllowedQps("foo"), 0.01);
    }
}
