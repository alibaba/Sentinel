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
package com.alibaba.csp.sentinel.transport.heartbeat;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 * @author Eric Zhao
 * @author leyou
 */
@SpiOrder(SpiOrder.LOWEST_PRECEDENCE - 100)
public class HttpHeartbeatSender implements HeartbeatSender {

    private final CloseableHttpClient client;

    private final int timeoutMs = 3000;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeoutMs)
        .setConnectTimeout(timeoutMs)
        .setSocketTimeout(timeoutMs)
        .build();

    private String consoleHost;
    private int consolePort;

    public HttpHeartbeatSender() {
        this.client = HttpClients.createDefault();
        List<Tuple2<String, Integer>> dashboardList = parseDashboardList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] No dashboard available");
        } else {
            consoleHost = dashboardList.get(0).r1;
            consolePort = dashboardList.get(0).r2;
            RecordLog.info("[NettyHttpHeartbeatSender] Dashboard address parsed: <" + consoleHost + ':' + consolePort + ">");
        }
    }

    protected static List<Tuple2<String, Integer>> parseDashboardList() {
        List<Tuple2<String, Integer>> list = new ArrayList<Tuple2<String, Integer>>();
        try {
            String ipsStr = TransportConfig.getConsoleServer();
            if (StringUtil.isBlank(ipsStr)) {
                RecordLog.warn("[NettyHttpHeartbeatSender] Dashboard server address is not configured");
                return list;
            }

            for (String ipPortStr : ipsStr.split(",")) {
                if (ipPortStr.trim().length() == 0) {
                    continue;
                }
                ipPortStr = ipPortStr.trim();
                if (ipPortStr.startsWith("http://")) {
                    ipPortStr = ipPortStr.substring(7);
                }
                if (ipPortStr.startsWith(":")) {
                    continue;
                }
                String[] ipPort = ipPortStr.trim().split(":");
                int port = 80;
                if (ipPort.length > 1) {
                    port = Integer.parseInt(ipPort[1].trim());
                }
                list.add(Tuple2.of(ipPort[0].trim(), port));
            }
        } catch (Exception ex) {
            RecordLog.warn("[NettyHttpHeartbeatSender] Parse dashboard list failed, current address list: " + list, ex);
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (StringUtil.isEmpty(consoleHost)) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
            .setPath("/registry/machine")
            .setParameter("app", AppNameUtil.getAppName())
            .setParameter("app_type", String.valueOf(SentinelConfig.getAppType()))
            .setParameter("v", Constants.SENTINEL_VERSION)
            .setParameter("version", String.valueOf(System.currentTimeMillis()))
            .setParameter("hostname", HostNameUtil.getHostName())
            .setParameter("ip", TransportConfig.getHeartbeatClientIp())
            .setParameter("port", TransportConfig.getPort())
            .setParameter("pid", String.valueOf(PidUtil.getPid()));

        HttpGet request = new HttpGet(uriBuilder.build());
        request.setConfig(requestConfig);
        // Send heartbeat request.
        CloseableHttpResponse response = client.execute(request);
        response.close();
        return true;
    }

    @Override
    public long intervalMs() {
        return 5000;
    }
}
