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
import com.alibaba.csp.sentinel.transport.config.KieConfig;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;
import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.Map;

/**
 * Heart beat message entity.
 * The message consists of key-value pair parameters.
 *
 * @author leyou
 */
public class KieHeartbeatMessage {
    private final Map<String, String> message = new HashMap<String, String>();

    public KieHeartbeatMessage() {
        message.put("hostname", HostNameUtil.getHostName());
        message.put("ip", TransportConfig.getHeartbeatClientIp());
        message.put("port", String.valueOf(TransportConfig.getPort()));

        // Kie Config
        message.put("app", KieConfig.getInstance().getApp());
        message.put("environment", KieConfig.getInstance().getEnvironment());
        message.put("project", KieConfig.getInstance().getProject());
        message.put("service", KieConfig.getInstance().getService());
        message.put("serverVersion", KieConfig.getInstance().getVersion());
    }

    public KieHeartbeatMessage registerInformation(String key, String value) {
        message.put(key, value);
        return this;
    }

    public String generateCurrentMessage() {
        // Version of Sentinel.
        message.put("sentinelVersion", Constants.SENTINEL_VERSION);
        // Actually timestamp.
        message.put("heartbeatVersion", String.valueOf(TimeUtil.currentTimeMillis()));
        message.put("port", String.valueOf(TransportConfig.getPort()));

        return JSON.toJSONString(message);
    }
}
