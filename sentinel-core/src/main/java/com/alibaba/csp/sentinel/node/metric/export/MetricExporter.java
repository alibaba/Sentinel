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

package com.alibaba.csp.sentinel.node.metric.export;

import com.alibaba.csp.sentinel.node.metric.MetricNode;

import java.util.List;
import java.util.Map;

/**
 * Used to export metric.
 *
 * @author Daydreamer-ia
 */
public interface MetricExporter {

    /**
     * export metrics.
     *
     * @param metrics metrics.
     */
    void export(Map<Long, List<MetricNode>> metrics);
}