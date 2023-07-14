package com.alibaba.csp.sentinel.prom;

import com.alibaba.csp.sentinel.init.InitFunc;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.prom.collector.SentinelCollector;
import io.prometheus.client.exporter.HTTPServer;

/**
 * The{@link PromExporterInit} the InitFunc for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class PromExporterInit implements InitFunc {

    private static final int promPort;

    static {
        promPort = Integer.parseInt(System.getProperty("sentinel.prometheus.port","20001"));
    }

    @Override
    public void init() throws Exception {
        HTTPServer server = null;
        try {
            new SentinelCollector().register();
            // 开启http服务供prometheus调用
            // 默认只提供一个接口 http://ip:port/metrics，返回所有指标
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
