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
package com.alibaba.csp.sentinel.slots.block.flow.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;

/**
 * @author jialiang.linjl
 */
public class WarmUpControllerTest extends AbstractTimeBasedTest {

    @Test
    public void testWarmUp() throws InterruptedException {
        WarmUpController warmupController = new WarmUpController(10, 10, 3);
        
        setCurrentMillis(System.currentTimeMillis());

        Node node = mock(Node.class);

        when(node.passQps()).thenReturn(8L);
        when(node.previousPassQps()).thenReturn(1L);

        assertFalse(warmupController.canPass(node, 1));

        when(node.passQps()).thenReturn(1L);
        when(node.previousPassQps()).thenReturn(1L);

        assertTrue(warmupController.canPass(node, 1));

        when(node.previousPassQps()).thenReturn(10L);

        for (int i = 0; i < 100; i++) {
            sleep(100);
            warmupController.canPass(node, 1);
        }
        when(node.passQps()).thenReturn(8L);
        assertTrue(warmupController.canPass(node, 1));

        when(node.passQps()).thenReturn(10L);
        assertFalse(warmupController.canPass(node, 1));
    }
}
