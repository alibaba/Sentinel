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

import com.alibaba.csp.sentinel.heartbeat.HeartbeatMessageProvider;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;
import com.alibaba.csp.sentinel.transport.endpoint.Protocol;
import com.alibaba.csp.sentinel.transport.heartbeat.client.HttpClientsFactory;
import com.alibaba.csp.sentinel.transport.message.HeartbeatMessage;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Zhao
 * @author Carpenter Lee
 * @author Leo Li
 */
@Spi(order = Spi.ORDER_LOWEST - 100)
public class HttpHeartbeatSender implements HeartbeatSender {

    private final CloseableHttpClient client;

    private static final int OK_STATUS = 200;

    private final int timeoutMs = 3000;
    private final RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(timeoutMs)
        .setConnectTimeout(timeoutMs)
        .setSocketTimeout(timeoutMs)
        .build();

    private final Protocol consoleProtocol;
    private final String consoleHost;
    private final int consolePort;

    private final HeartbeatMessage heartbeatMessage = HeartbeatMessageProvider.getHeartbeatMessage();

    public HttpHeartbeatSender() {
        List<Endpoint> dashboardList = TransportConfig.getConsoleServerList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] No dashboard server available");
            consoleProtocol = Protocol.HTTP;
            consoleHost = null;
            consolePort = -1;
        } else {
            consoleProtocol = dashboardList.get(0).getProtocol();
            consoleHost = dashboardList.get(0).getHost();
            consolePort = dashboardList.get(0).getPort();
            RecordLog.info("[NettyHttpHeartbeatSender] Dashboard address parsed: <{}:{}>", consoleHost, consolePort);
        }
        this.client = HttpClientsFactory.getHttpClientsByProtocol(consoleProtocol);
    }

    private List<NameValuePair> resolveParameters() {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        Map<String, String> map = this.heartbeatMessage.get();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            NameValuePair nameValuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
            nameValuePairs.add(nameValuePair);
        }
        return nameValuePairs;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (StringUtil.isEmpty(consoleHost)) {
            return false;
        }
        List<NameValuePair> parameters = this.resolveParameters();
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(consoleProtocol.getProtocol()).setHost(consoleHost).setPort(consolePort)
            .setPath(TransportConfig.getHeartbeatApiPath())
            .setParameters(parameters);

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
