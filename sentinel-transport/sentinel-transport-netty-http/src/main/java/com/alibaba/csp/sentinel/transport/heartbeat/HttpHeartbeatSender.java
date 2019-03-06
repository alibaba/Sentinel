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
import com.alibaba.csp.sentinel.spi.SpiOrder;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
<<<<<<< HEAD
import com.alibaba.csp.sentinel.transport.config.DashboardConfig;
=======
import com.alibaba.csp.sentinel.transport.config.HeartbeatConfigEntity;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
>>>>>>> change DashbordConfig to HeartbeatConfigEntity
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.ArrayList;
import java.util.List;

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

    private HeartbeatConfigEntity heartbeatConfigEntity;

    public HttpHeartbeatSender() {
        this.client = HttpClients.createDefault();
        List<HeartbeatConfigEntity> dashboardList = parseDashboardList();
        if (dashboardList == null || dashboardList.isEmpty()) {
            RecordLog.info("[NettyHttpHeartbeatSender] No dashboard available");
        } else {
            heartbeatConfigEntity = dashboardList.get(0);
            RecordLog.info("[NettyHttpHeartbeatSender] Dashboard address parsed: <" + heartbeatConfigEntity.getHost()
                    + ':' + heartbeatConfigEntity.getPort() + "/" + heartbeatConfigEntity.getPath() +">");
        }
    }

    private List<HeartbeatConfigEntity> parseDashboardList() {
        List<HeartbeatConfigEntity> list = new ArrayList<HeartbeatConfigEntity>();
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
                list.add(new HeartbeatConfigEntity(ipPortStr));
            }
        } catch (Exception ex) {
            RecordLog.warn("[NettyHttpHeartbeatSender] Parse dashboard list failed, current address list: " + list, ex);
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (heartbeatConfigEntity == null) {
            return false;
        }
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(heartbeatConfigEntity.getSchema()).setHost(heartbeatConfigEntity.getHost())
            .setPort(heartbeatConfigEntity.getPort()).setPath(heartbeatConfigEntity.getPath()+"/registry/machine")
            .setParameter("app", AppNameUtil.getAppName())
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
