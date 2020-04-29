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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;

/**
 * @author Eric Zhao
 * @author Carpenter Lee
 */
@SpiOrder(SpiOrder.LOWEST_PRECEDENCE - 100)
public class HttpHeartbeatSender implements HeartbeatSender {

    private final CloseableHttpClient client;

    private static final int OK_STATUS = 200;

    private final int timeoutMs = 3000;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeoutMs)
        .setConnectTimeout(timeoutMs)
        .setSocketTimeout(timeoutMs)
        .build();

    private final String consoleHost;
    private final int consolePort;

    public HttpHeartbeatSender() {
        this.client = HttpClients.createDefault();
        List<Tuple2<String, Integer>> dashboardList = TransportConfig.getConsoleServerList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] No dashboard server available");
            consoleHost = null;
            consolePort = -1;
        } else {
            consoleHost = dashboardList.get(0).r1;
            consolePort = dashboardList.get(0).r2;
            RecordLog.info(
                "[NettyHttpHeartbeatSender] Dashboard address parsed: <" + consoleHost + ':' + consolePort + ">");
        }
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (StringUtil.isEmpty(consoleHost)) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme("http").setHost(consoleHost).setPort(consolePort)
            .setPath(TransportConfig.getHeartbeatApiPath())
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
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == OK_STATUS) {
            return true;
        } else if (clientErrorCode(statusCode) || serverErrorCode(statusCode)) {
            RecordLog.warn("[HttpHeartbeatSender] Failed to send heartbeat to "
                + consoleHost + ":" + consolePort + ", http status code: " + statusCode);
        }

        return false;
    }

    @Override
    public long intervalMs() {
        return 5000;
    }

    private boolean clientErrorCode(int code) {
        return code > 399 && code < 500;
    }

    private boolean serverErrorCode(int code) {
        return code > 499 && code < 600;
    }
}
