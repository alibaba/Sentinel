/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.metric.prom;

/**
 * The{@link PromExporterInit} the Collector for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-08-08 09:30
 * @since 2.0.0
 */
public final class MetricConstants {

    public static final String METRIC_HELP = "sentinel_metrics";

    public static final String RESOURCE = "resource";

    public static final String CLASSIFICATION = "classification";

    public static final String METRIC_TYPE = "type";

    public static final String PASS_QPS = "passQps";

    public static final String BLOCK_QPS = "blockQps";

    public static final String SUCCESS_QPS = "successQps";

    public static final String EXCEPTION_QPS = "exceptionQps";

    public static final String RT = "rt";

    public static final String OCC_PASS_QPS = "occupiedPassQps";

    public static final String CONCURRENCY = "concurrency";

    private MetricConstants() {
    }
}
