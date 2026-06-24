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
package com.alibaba.csp.sentinel.node.metric;

import com.alibaba.csp.sentinel.ResourceTypeConstants;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class MetricNodeTest {

    @Test
    public void testFromFatString() {
        String line = "1564382218000|2019-07-29 14:36:58|/foo/*|1|0|1|0|0|0|2|1";
        MetricNode node = MetricNode.fromFatString(line);
        assertEquals(ResourceTypeConstants.COMMON_WEB, node.getClassification());
        assertEquals(2, node.getConcurrency());
        assertEquals(1, node.getSuccessQps());
    }

    @Test
    public void testFromThinStringShortInput() {
        // Input with only 3 pipe-separated fields (fewer than the required 7).
        // The method should gracefully handle missing fields and NOT throw
        // ArrayIndexOutOfBoundsException, consistent with how later fields
        // (strs[7], strs[8], strs[9]) already have length guards.
        String line = "1564382218000|resource|10";
        MetricNode node = MetricNode.fromThinString(line);
        assertEquals(1564382218000L, node.getTimestamp());
        assertEquals("resource", node.getResource());
        assertEquals(10L, node.getPassQps());
    }
}
