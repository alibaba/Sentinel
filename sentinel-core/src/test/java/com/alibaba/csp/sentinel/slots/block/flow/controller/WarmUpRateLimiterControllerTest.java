/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpRateLimiterController;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author CarpenterLee
 */
public class WarmUpRateLimiterControllerTest {

    @Test
    public void testPace() throws InterruptedException {
        WarmUpRateLimiterController controller = new WarmUpRateLimiterController(10, 10, 1000, 3);

        Node node = mock(Node.class);

        when(node.passQps()).thenReturn(100L);
        when(node.previousPassQps()).thenReturn(100L);

        assertTrue(controller.canPass(node, 1));

        long start = System.currentTimeMillis();
        assertTrue(controller.canPass(node, 1));
        long cost = System.currentTimeMillis() - start;
        assertTrue(cost >= 100 && cost <= 110);
    }

    @Test
    public void testPaceCanNotPass() throws InterruptedException {
        WarmUpRateLimiterController controller = new WarmUpRateLimiterController(10, 10, 10, 3);

        Node node = mock(Node.class);

        when(node.passQps()).thenReturn(100L);
        when(node.previousPassQps()).thenReturn(100L);

        assertTrue(controller.canPass(node, 1));

        assertFalse(controller.canPass(node, 1));
    }
}