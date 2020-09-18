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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.transport.heartbeat.client.HttpClient;
import com.alibaba.csp.sentinel.transport.heartbeat.client.HttpResponse;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import java.util.List;

/**
 * The heartbeat sender provides basic API for sending heartbeat request to provided target.
 * This implementation is based on a trivial HTTP client.
 *
 * @author Eric Zhao
 * @author Carpenter Lee
 */
public class KieHttpHeartbeatSender implements HeartbeatSender {

    public static final String HEARTBEAT_DEFAULT_PATH = "/registry/kieServer";

    private static final int OK_STATUS = 200;

    private static final long DEFAULT_INTERVAL = 1000 * 10;

    private final KieHeartbeatMessage heartBeat = new KieHeartbeatMessage();

    private final List<Tuple2<String, Integer>> addressList;

    private int currentAddressIdx = 0;

    public KieHttpHeartbeatSender() {
        // Retrieve the list of default addresses.
        List<Tuple2<String, Integer>> newAddrs = TransportConfig.getConsoleServerList();
        if (newAddrs.isEmpty()) {
            RecordLog.warn("[KieHttpHeartbeatSender] Dashboard server address not configured or not available");
        } else {
            RecordLog.info("[KieHttpHeartbeatSender] Default console address list retrieved: " + newAddrs);
        }
        this.addressList = newAddrs;
    }

    private String getHeartbeatUrl(){
        String address = getAvailableAddress();
        return "http://" + address + "/" + HEARTBEAT_DEFAULT_PATH;
    }

    @Override
    public boolean sendHeartbeat(){
        String heartbeatUrl = getHeartbeatUrl();
        String heartbeatMsg = heartBeat.generateCurrentMessage();

        RecordLog.info(String.format("Send heartbeat %s to %s.", heartbeatMsg, heartbeatUrl));
        try {
            HttpResponse response = HttpClient.post(heartbeatUrl, heartbeatMsg);

            if (response.getStatusCode() == OK_STATUS) {
                return true;
            } else if (clientErrorCode(response.getStatusCode()) || serverErrorCode(response.getStatusCode())) {
                RecordLog.warn("[KieHttpHeartbeatSender] Failed to send heartbeat to " + heartbeatUrl
                        + ", http status code: " + response.getStatusCode());
            }
        }catch (RuntimeException e){
            RecordLog.error("[KieHttpHeartbeatSender] Failed to send heartbeat to " + heartbeatUrl, e);
        }
        return false;
    }

    @Override
    public long intervalMs() {
        return DEFAULT_INTERVAL;
    }

    private String getAvailableAddress() {
        if (addressList == null || addressList.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % addressList.size();
        return addressList.get(index).r1 + ":" + addressList.get(index).r2;
    }

    private boolean clientErrorCode(int code) {
        return code > 399 && code < 500;
    }

    private boolean serverErrorCode(int code) {
        return code > 499 && code < 600;
    }
}
