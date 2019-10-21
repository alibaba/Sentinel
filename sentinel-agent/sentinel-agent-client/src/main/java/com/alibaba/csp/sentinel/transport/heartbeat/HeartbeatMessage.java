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
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Heart beat message entity.
 * The message consists of key-value pair parameters.
 *
 * @author leyou
 */
public class HeartbeatMessage {

    private final Map<String, String> message = new HashMap<String, String>();

    public HeartbeatMessage() {
        message.put("hostname", HostNameUtil.getHostName());
        message.put("ip", TransportConfig.getHeartbeatClientIp());
        message.put("app", AppNameUtil.getAppName());
        // Put application type (since 1.6.0).
        message.put("app_type", String.valueOf(SentinelConfig.getAppType()));
        message.put("port", String.valueOf(TransportConfig.getPort()));
    }

    public HeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    public Map<String, String> generateCurrentMessage() {
        // Version of Sentinel.
        message.put("v", Constants.SENTINEL_VERSION);
        // Actually timestamp.
        message.put("version", String.valueOf(TimeUtil.currentTimeMillis()));
        message.put("port", String.valueOf(TransportConfig.getPort()));
        return message;
    }
}
