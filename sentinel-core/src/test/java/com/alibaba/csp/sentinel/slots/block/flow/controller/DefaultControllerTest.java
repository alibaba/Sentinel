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
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.TrafficShapingController;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Eric Zhao
 */
public class DefaultControllerTest {

    @Test
    public void testCanPassForQps() {
        long threshold = 10;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_QPS);
        Node node = mock(Node.class);
        when(node.passQps()).thenReturn(threshold - 1)
            .thenReturn(threshold);

        assertTrue(controller.canPass(node, 1));
        assertFalse(controller.canPass(node, 1));
    }

    @Test
    public void testCanPassForThreadCount() {
        int threshold = 8;
        TrafficShapingController controller = new DefaultController(threshold, RuleConstant.FLOW_GRADE_THREAD);
        Node node = mock(Node.class);
        when(node.curThreadNum()).thenReturn(threshold - 1)
            .thenReturn(threshold);

        assertTrue(controller.canPass(node, 1));
        assertFalse(controller.canPass(node, 1));
    }

    @Test
    public void testCanPassForQpsMultiThread() {
    }
}