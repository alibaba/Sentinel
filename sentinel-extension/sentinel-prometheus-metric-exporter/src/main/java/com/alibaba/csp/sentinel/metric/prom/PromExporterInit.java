/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.prom;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.metric.prom.collector.SentinelCollector;
import com.alibaba.csp.sentinel.metric.prom.config.PrometheusGlobalConfig;
import io.prometheus.client.exporter.HTTPServer;

/**
 * The{@link PromExporterInit} the InitFunc for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class PromExporterInit implements InitFunc {

    @Override
    public void init() throws Exception {
        HTTPServer server = null;
        try {
            new SentinelCollector().register();
            // 开启http服务供prometheus调用
            // 默认只提供一个接口 http://ip:port/metrics，返回所有指标
            int promPort = PrometheusGlobalConfig.getPromFetchPort();
            server = new HTTPServer(promPort);
        } catch (Throwable e) {
            RecordLog.warn("[PromExporterInit] failed to init prometheus exporter with exception:", e);
        }

        HTTPServer finalServer = server;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (finalServer != null) {
                finalServer.stop();
            }
        }));
    }

}
