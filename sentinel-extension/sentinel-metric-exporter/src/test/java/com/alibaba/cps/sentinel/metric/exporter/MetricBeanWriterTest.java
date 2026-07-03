/*
 *  Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.alibaba.cps.sentinel.metric.exporter;

import com.alibaba.csp.sentinel.metric.exporter.jmx.MetricBeanWriter;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@link com.alibaba.csp.sentinel.metric.exporter.jmx.MetricBeanWriter} unit test
 *
 * @author quguai
 * @date 2022/12/7 21:33
 */
public class MetricBeanWriterTest {

    @Test
    public void testEscapeSpecialCharacter() {
        String character = MetricBeanWriter.escapeSpecialCharacter(null);
        Assert.assertNull(character);

        character = MetricBeanWriter.escapeSpecialCharacter("");
        Assert.assertEquals("", character);

        character = MetricBeanWriter.escapeSpecialCharacter("sentinel");
        Assert.assertEquals("sentinel", character);

        character = MetricBeanWriter.escapeSpecialCharacter("*sentinel");
        Assert.assertEquals("\"\\*sentinel\"", character);

        character = MetricBeanWriter.escapeSpecialCharacter("?sentinel");
        Assert.assertEquals("\"\\?sentinel\"", character);

        character = MetricBeanWriter.escapeSpecialCharacter("\nsentinel");
        Assert.assertEquals("\"\\nsentinel\"", character);

        character = MetricBeanWriter.escapeSpecialCharacter("\"sentinel");
        Assert.assertEquals("\"\\\"sentinel\"", character);

        character = MetricBeanWriter.escapeSpecialCharacter("=sentinel");
        Assert.assertEquals("\"=sentinel\"", character);

        character = MetricBeanWriter.escapeSpecialCharacter(":sentinel");
        Assert.assertEquals("\":sentinel\"", character);
    }
}
