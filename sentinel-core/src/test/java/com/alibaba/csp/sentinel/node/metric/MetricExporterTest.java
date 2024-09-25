/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

/**
 * @author Daydreamer-ia.
 */
public class MetricExporterTest {

    @Test
    public void testExporter() throws NoSuchFieldException, IllegalAccessException {
        Field exporter = MetricTimerListener.class.getDeclaredField("exporter");
        exporter.setAccessible(true);
        Object o = exporter.get(exporter);
        assertTrue(o instanceof TestMetricExporter);
    }

}