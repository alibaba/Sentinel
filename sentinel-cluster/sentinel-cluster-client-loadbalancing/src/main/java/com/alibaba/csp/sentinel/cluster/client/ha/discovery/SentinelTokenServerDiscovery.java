/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.client.ha.discovery;

import com.alibaba.csp.sentinel.cluster.TokenServerDescriptor;
import com.alibaba.csp.sentinel.cluster.client.ha.TokenServerDiscovery;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.spi.Spi;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author icodening
 * @date 2022.03.06
 */
@Spi(value = SentinelTokenServerDiscovery.NAME)
public class SentinelTokenServerDiscovery implements TokenServerDiscovery {

    public static final String NAME = "sentinel";

    private static final String CONSOLE_SERVER = "csp.sentinel.dashboard.server";

    private static final int OK_STATUS = 200;

    private final List<String> consoleServerURLs;

    private int currentAddressIdx = 0;

    public SentinelTokenServerDiscovery() {
        this(getConsoleServerList());
    }

    public SentinelTokenServerDiscovery(String... addresses) {
        this(Arrays.asList(addresses));
    }

    public SentinelTokenServerDiscovery(List<String> consoleServerURLs) {
        this.consoleServerURLs = consoleServerURLs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TokenServerDescriptor> getTokenServers(String appName) {
        String serverURL = getAvailableAddress();
        if (serverURL == null) {
            return Collections.emptyList();
        }
        try {
            String api = "/cluster/server_state/" + appName;
            String requestURL = serverURL + api;
            URL url = new URL(requestURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(5000);
            if (httpURLConnection.getResponseCode() == OK_STATUS) {
                byte[] tmp = new byte[4096];
                ByteArrayOutputStream bos = new ByteArrayOutputStream(8192);
                InputStream inputStream = httpURLConnection.getInputStream();
                int len;
                while ((len = inputStream.read(tmp)) != -1) {
                    bos.write(tmp, 0, len);
                }
                JSONObject consoleResult = JSON.parseObject(bos.toString());
                JSONArray apps = consoleResult.getJSONArray("data");
                List<TokenServerDescriptor> tokenServerDescriptors = new ArrayList<>(apps.size());
                for (int i = 0; i < apps.size(); i++) {
                    JSONObject app = apps.getJSONObject(i);
                    String ip = app.getString("ip");
                    int port = app.getIntValue("port");
                    tokenServerDescriptors.add(new TokenServerDescriptor(ip, port));
                }
                return tokenServerDescriptors;
            } else {
                RecordLog.warn("[SentinelTokenServerDiscovery] Failed to fetch token server from remote[{}] Sentinel Dashboard, error message: {}", requestURL, httpURLConnection.getResponseMessage());
            }
        } catch (IOException e) {
            RecordLog.error("[SentinelTokenServerDiscovery] Failed to fetch token server from remote Sentinel Dashboard !!!", e);
        }
        return Collections.emptyList();
    }

    private String getAvailableAddress() {
        if (consoleServerURLs == null || consoleServerURLs.isEmpty()) {
            return null;
        }
        if (currentAddressIdx < 0) {
            currentAddressIdx = 0;
        }
        int index = currentAddressIdx % consoleServerURLs.size();
        return consoleServerURLs.get(index);
    }

    public static List<String> getConsoleServerList() {
        String config = SentinelConfig.getConfig(CONSOLE_SERVER);
        List<String> list = new ArrayList<>();
        if (StringUtil.isBlank(config)) {
            return list;
        }
        int pos;
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
                cur++;
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
            String protocol = "http";
            if (ipPortStr.startsWith("http://")) {
                ipPortStr = ipPortStr.substring(7);
            } else if (ipPortStr.startsWith("https://")) {
                ipPortStr = ipPortStr.substring(8);
                port = 443;
                protocol = "https";
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
            list.add(protocol + "://" + host + ":" + port);
        }
        return list;
    }

}

