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
package com.alibaba.csp.sentinel.transport.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.transport.endpoint.Endpoint;
import com.alibaba.csp.sentinel.transport.endpoint.Protocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carpenter Lee
 * @author Jason Joo
 * @author Leo Li
 */
public class TransportConfig {

    public static final String CONSOLE_SERVER = "csp.sentinel.dashboard.server";
    public static final String SERVER_PORT = "csp.sentinel.api.port";
    public static final String HEARTBEAT_INTERVAL_MS = "csp.sentinel.heartbeat.interval.ms";
    public static final String HEARTBEAT_CLIENT_IP = "csp.sentinel.heartbeat.client.ip";
    public static final String HEARTBEAT_API_PATH = "csp.sentinel.heartbeat.api.path";

    public static final String HEARTBEAT_DEFAULT_PATH = "/registry/machine";

    private static int runtimePort = -1;

    /**
     * Get heartbeat interval in milliseconds.
     *
     * @return heartbeat interval in milliseconds if exists, or null if not configured or invalid config
     */
    public static Long getHeartbeatIntervalMs() {
        String interval = SentinelConfig.getConfig(HEARTBEAT_INTERVAL_MS);
        try {
            return interval == null ? null : Long.parseLong(interval);
        } catch (Exception ex) {
            RecordLog.warn("[TransportConfig] Failed to parse heartbeat interval: " + interval);
            return null;
        }
    }

    /**
     * Get a list of Endpoint(protocol, ip/domain, port) indicating Sentinel Dashboard's address.<br>
     * NOTE: only support <b>HTTP</b> and <b>HTTPS</b> protocol
     *
     * @return list of Endpoint(protocol, ip/domain, port). <br>
     *         <b>May not be null</b>. <br>
     *         An empty list returned when not configured.
     */
    public static List<Endpoint> getConsoleServerList() {
        String config = SentinelConfig.getConfig(CONSOLE_SERVER);
        List<Endpoint> list = new ArrayList<Endpoint>();
        if (StringUtil.isBlank(config)) {
            return list;
        }

        int pos = -1;
        int cur = 0;
        while (true) {
            pos = config.indexOf(',', cur);
            if (cur < config.length() - 1 && pos < 0) {
                // for single segment, pos move to the end
                pos = config.length();
            }
            if (pos < 0) {
                break;
            }
            if (pos <= cur) {
                cur ++;
                continue;
            }
            // parsing
            String ipPortStr = config.substring(cur, pos);
            cur = pos + 1;
            if (StringUtil.isBlank(ipPortStr)) {
                continue;
            }
            ipPortStr = ipPortStr.trim();
            int port = 80;
            Protocol protocol = Protocol.HTTP;
            if (ipPortStr.startsWith("http://")) {
                ipPortStr = ipPortStr.substring(7);
            } else if (ipPortStr.startsWith("https://")) {
                ipPortStr = ipPortStr.substring(8);
                port = 443;
                protocol = Protocol.HTTPS;
            }
            int index = ipPortStr.indexOf(":");
            if (index == 0) {
                // skip
                continue;
            }
            String host = ipPortStr;
            if (index >= 0) {
                try {
                    port = Integer.parseInt(ipPortStr.substring(index + 1));
                    if (port <= 1 || port >= 65535) {
                        throw new RuntimeException("Port number [" + port + "] over range");
                    }
                } catch (Exception e) {
                    RecordLog.warn("Parse port of dashboard server failed: " + ipPortStr, e);
                    // skip
                    continue;
                }
                host = ipPortStr.substring(0, index);
            }
            list.add(new Endpoint(protocol, host, port));
        }
        return list;
    }

    public static int getRuntimePort() {
        return runtimePort;
    }

    /**
     * Get Server port of this HTTP server.
     *
     * @return the port, maybe null if not configured.
     */
    public static String getPort() {
        if (runtimePort > 0) {
            return String.valueOf(runtimePort);
        }
        return SentinelConfig.getConfig(SERVER_PORT);
    }

    /**
     * Set real port this HTTP server uses.
     *
     * @param port real port.
     */
    public static void setRuntimePort(int port) {
        runtimePort = port;
    }

    /**
     * Get heartbeat client local ip.
     * If the client ip not configured,it will be the address of local host
     *
     * @return the local ip.
     */
    public static String getHeartbeatClientIp() {
        String ip = SentinelConfig.getConfig(HEARTBEAT_CLIENT_IP);
        if (StringUtil.isBlank(ip)) {
            ip = HostNameUtil.getIp();
        }
        return ip;
    }

    /**
     * Get the heartbeat api path. If the machine registry path of the dashboard
     * is modified, then the API path should also be consistent with the API path of the dashboard.
     *
     * @return the heartbeat api path
     * @since 1.7.1
     */
    public static String getHeartbeatApiPath() {
        String apiPath = SentinelConfig.getConfig(HEARTBEAT_API_PATH);
        if (StringUtil.isBlank(apiPath)) {
            return HEARTBEAT_DEFAULT_PATH;
        }
        if (!apiPath.startsWith("/")) {
            apiPath = "/" + apiPath;
        }
        return apiPath;
    }
}
