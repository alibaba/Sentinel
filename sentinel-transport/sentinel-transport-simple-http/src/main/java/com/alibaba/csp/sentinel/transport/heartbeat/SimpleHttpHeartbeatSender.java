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

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpClient;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpRequest;
import com.alibaba.csp.sentinel.transport.heartbeat.client.SimpleHttpResponse;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * The heartbeat sender provides basic API for sending heartbeat request to provided target.
 * This implementation is based on a trivial HTTP client.
 *
 * @author Eric Zhao
 * @author leyou
 */
public class SimpleHttpHeartbeatSender implements HeartbeatSender {

    private static final String HEARTBEAT_PATH = "/registry/machine";
    private static final int OK_STATUS = 200;

    private static final long DEFAULT_INTERVAL = 1000 * 10;

    private final HeartbeatMessage heartBeat = new HeartbeatMessage();
    private final SimpleHttpClient httpClient = new SimpleHttpClient();

    private final List<InetSocketAddress> addressList;

    private int currentAddressIdx = 0;

    public SimpleHttpHeartbeatSender() {
        // Retrieve the list of default addresses.
        List<InetSocketAddress> newAddrs = getDefaultConsoleIps();
        RecordLog.info("[SimpleHttpHeartbeatSender] Default console address list retrieved: " + newAddrs);
        this.addressList = newAddrs;
    }

    @Override
    public boolean sendHeartbeat() throws Exception {
        if (TransportConfig.getRuntimePort() <= 0) {
            RecordLog.info("[SimpleHttpHeartbeatSender] Runtime port not initialized, won't send heartbeat");
            return false;
        }
        InetSocketAddress addr = getAvailableAddress();
        if (addr == null) {
            return false;
        }

        SimpleHttpRequest request = new SimpleHttpRequest(addr, HEARTBEAT_PATH);
        request.setParams(heartBeat.generateCurrentMessage());
        try {
            SimpleHttpResponse response = httpClient.post(request);
            if (response.getStatusCode() == OK_STATUS) {
                return true;
            }
        } catch (Exception e) {
            RecordLog.warn("[SimpleHttpHeartbeatSender] Failed to send heartbeat to " + addr + " : ", e);
        }
        return false;
    }

    @Override
    public long intervalMs() {
        return DEFAULT_INTERVAL;
    }

    private InetSocketAddress getAvailableAddress() {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % addressList.size();
        return addressList.get(index);
    }

    private List<InetSocketAddress> getDefaultConsoleIps() {
        List<InetSocketAddress> newAddrs = new ArrayList<InetSocketAddress>();
        try {
            String ipsStr = TransportConfig.getConsoleServer();
            if (StringUtil.isEmpty(ipsStr)) {
                RecordLog.warn("[SimpleHttpHeartbeatSender] Dashboard server address not configured");
                return newAddrs;
            }

            for (String ipPortStr : ipsStr.split(",")) {
                if (ipPortStr.trim().length() == 0) {
                    continue;
                }
                if (ipPortStr.startsWith("http://")) {
                    ipPortStr = ipPortStr.trim().substring(7);
                }
                String[] ipPort = ipPortStr.trim().split(":");
                int port = 80;
                if (ipPort.length > 1) {
                    port = Integer.parseInt(ipPort[1].trim());
                }
                newAddrs.add(new InetSocketAddress(ipPort[0].trim(), port));
            }
        } catch (Exception ex) {
            RecordLog.warn("[SimpleHeartbeatSender] Parse dashboard list failed, current address list: " + newAddrs, ex);
            ex.printStackTrace();
        }
        return newAddrs;
    }
}
