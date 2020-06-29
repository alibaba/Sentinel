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
package com.alibaba.csp.sentinel.datasource.eureka;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * A {@link ReadableDataSource} based on Eureka. This class will automatically
 * fetches the metadata of the instance every period.
 * </p>
 * <p>
 * Limitations: Default refresh interval is 10s. Because there is synchronization between eureka servers,
 * it may take longer to take effect.
 * </p>
 *
 * @author: liyang
 * @create: 2020-05-23 12:01
 */
public class EurekaDataSource<T> extends AutoRefreshDataSource<String, T> {

    private static final long DEFAULT_REFRESH_MS = 10000;

    /**
     * connect timeout: 3s
     */
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

    /**
     * read timeout: 30s
     */
    private static final int DEFAULT_READ_TIMEOUT_MS = 30000;


    private int connectTimeoutMills;


    private int readTimeoutMills;

    /**
     * eureka instance appid
     */
    private String appId;
    /**
     * eureka instance id
     */
    private String instanceId;

    /**
     * collect of eureka server urls
     */
    private List<String> serviceUrls;

    /**
     * metadata key of the rule source
     */
    private String ruleKey;


    public EurekaDataSource(String appId, String instanceId, List<String> serviceUrls, String ruleKey,
                            Converter<String, T> configParser) {
        this(appId, instanceId, serviceUrls, ruleKey, configParser, DEFAULT_REFRESH_MS, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }


    public EurekaDataSource(String appId, String instanceId, List<String> serviceUrls, String ruleKey,
                            Converter<String, T> configParser, long refreshMs, int connectTimeoutMills,
                            int readTimeoutMills) {
        super(configParser, refreshMs);
        AssertUtil.notNull(appId, "appId can't be null");
        AssertUtil.notNull(instanceId, "instanceId can't be null");
        AssertUtil.assertNotEmpty(serviceUrls, "serviceUrls can't be empty");
        AssertUtil.notNull(ruleKey, "ruleKey can't be null");
        AssertUtil.assertState(connectTimeoutMills > 0, "connectTimeoutMills must be greater than 0");
        AssertUtil.assertState(readTimeoutMills > 0, "readTimeoutMills must be greater than 0");

        this.appId = appId;
        this.instanceId = instanceId;
        this.serviceUrls = ensureEndWithSlash(serviceUrls);
        AssertUtil.assertNotEmpty(this.serviceUrls, "No available service url");
        this.ruleKey = ruleKey;
        this.connectTimeoutMills = connectTimeoutMills;
        this.readTimeoutMills = readTimeoutMills;
    }


    private List<String> ensureEndWithSlash(List<String> serviceUrls) {
        List<String> newServiceUrls = new ArrayList<>();
        for (String serviceUrl : serviceUrls) {
            if (StringUtil.isBlank(serviceUrl)) {
                continue;
            }
            if (!serviceUrl.endsWith("/")) {
                serviceUrl = serviceUrl + "/";
            }
            newServiceUrls.add(serviceUrl);
        }
        return newServiceUrls;
    }

    @Override
    public String readSource() throws Exception {
        return fetchStringSourceFromEurekaMetadata(this.appId, this.instanceId, this.serviceUrls, ruleKey);
    }


    private String fetchStringSourceFromEurekaMetadata(String appId, String instanceId, List<String> serviceUrls,
                                                       String ruleKey) throws Exception {
        List<String> shuffleUrls = new ArrayList<>(serviceUrls.size());
        shuffleUrls.addAll(serviceUrls);
        Collections.shuffle(shuffleUrls);
        for (int i = 0; i < shuffleUrls.size(); i++) {
            String serviceUrl = shuffleUrls.get(i) + String.format("apps/%s/%s", appId, instanceId);
            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) new URL(serviceUrl).openConnection();
                conn.addRequestProperty("Accept", "application/json;charset=utf-8");

                conn.setConnectTimeout(connectTimeoutMills);
                conn.setReadTimeout(readTimeoutMills);
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.connect();
                RecordLog.debug("[EurekaDataSource] Request from eureka server: " + serviceUrl);
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String s = toString(conn.getInputStream());
                    String ruleString = JSON.parseObject(s)
                            .getJSONObject("instance")
                            .getJSONObject("metadata")
                            .getString(ruleKey);
                    return ruleString;
                }
                RecordLog.warn("[EurekaDataSource] Warn: retrying on another server if available " +
                        "due to response code: {}, response message: {}", conn.getResponseCode(), toString(conn.getErrorStream()));
            } catch (Exception e) {
                try {
                    if (conn != null) {
                        RecordLog.warn("[EurekaDataSource] Warn: failed to request " + conn.getURL() + " from "
                                + InetAddress.getByName(conn.getURL().getHost()).getHostAddress(), e);
                    }
                } catch (Exception e1) {
                    RecordLog.warn("[EurekaDataSource] Warn: failed to request ", e1);
                    //ignore
                }
                RecordLog.warn("[EurekaDataSource] Warn: failed to request,retrying on another server if available");

            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        throw new EurekaMetadataFetchException("Can't get any data");
    }


    public static class EurekaMetadataFetchException extends Exception {

        public EurekaMetadataFetchException(String message) {
            super(message);
        }
    }


    private String toString(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(input, "utf-8");
        CharArrayWriter sw = new CharArrayWriter();
        copy(inputStreamReader, sw);
        return sw.toString();
    }

    private long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[1 << 12];
        long count = 0;
        for (int n = 0; (n = input.read(buffer)) >= 0; ) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


}
