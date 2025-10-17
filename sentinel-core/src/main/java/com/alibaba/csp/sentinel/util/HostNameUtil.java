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
package com.alibaba.csp.sentinel.util;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;

import java.net.InetAddress;
import java.util.List;

/**
 * Get host name and ip of the host.
 *
 * @author leyou
 * @author imlzw
 */
public final class HostNameUtil {

    public static final String HEARTBEAT_CLIENT_NET_IGNOREDINTERFACES = "csp.sentinel.heartbeat.client.net.ignoredInterfaces";
    public static final String HEARTBEAT_CLIENT_NET_PREFERREDNETWORKS = "csp.sentinel.heartbeat.client.net.preferredNetworks";

    private static String ip;
    private static String hostName;

    static {
        try {
            // Init the host information.
            resolveHost();
        } catch (Exception e) {
            RecordLog.info("Failed to get local host", e);
        }
    }

    private HostNameUtil() {
    }

    private static void resolveHost() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        hostName = addr.getHostName();
        ip = addr.getHostAddress();
        NetUtil.NetConfig config = new NetUtil.NetConfig();
        config.setIgnoredInterfaces(SentinelConfig.getConfig(HEARTBEAT_CLIENT_NET_IGNOREDINTERFACES));
        config.setPreferredNetworks(SentinelConfig.getConfig(HEARTBEAT_CLIENT_NET_PREFERREDNETWORKS));
        List<String[]> localIPs = NetUtil.getLocalIPs(config);
        if (localIPs != null && !localIPs.isEmpty()) {
            ip = localIPs.get(0)[1];
        }
    }

    public static String getIp() {
        return ip;
    }

    public static String getHostName() {
        return hostName;
    }

    public static String getConfigString() {
        return "{\n"
                + "\t\"machine\": \"" + hostName + "\",\n"
                + "\t\"ip\": \"" + ip + "\"\n"
                + "}";
    }
}