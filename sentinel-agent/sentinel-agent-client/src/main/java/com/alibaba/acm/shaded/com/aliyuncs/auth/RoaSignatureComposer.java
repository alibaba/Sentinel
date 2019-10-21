/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.acm.shaded.com.aliyuncs.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.acm.shaded.com.aliyuncs.http.FormatType;
import com.alibaba.acm.shaded.com.aliyuncs.http.MethodType;
import com.alibaba.acm.shaded.com.aliyuncs.utils.ParameterHelper;

public class RoaSignatureComposer implements ISignatureComposer {

    private static ISignatureComposer composer = null;
    protected final static String QUERY_SEPARATOR = "&";
    protected final static String HEADER_SEPARATOR = "\n";

    @Override
    public Map<String, String> refreshSignParameters(Map<String, String> parameters,
                                                     Signer signer, String accessKeyId, FormatType format) {
        Map<String, String> immutableMap = new HashMap<String, String>(parameters);
        immutableMap.put("Date", ParameterHelper.getRFC2616Date(null));
        if (null == format) { format = FormatType.RAW; }
        immutableMap.put("Accept", FormatType.mapFormatToAccept(format));
        immutableMap.put("x-acs-signature-method", signer.getSignerName());
        immutableMap.put("x-acs-signature-version", signer.getSignerVersion());
        if (signer.getSignerType() != null) {
            immutableMap.put("x-acs-signature-type", signer.getSignerType());
        }
        return immutableMap;
    }

    private String[] splitSubResource(String uri) {
        int queIndex = uri.indexOf("?");
        String[] uriParts = new String[2];
        if (-1 != queIndex) {
            uriParts[0] = uri.substring(0, queIndex);
            uriParts[1] = uri.substring(queIndex + 1);
        } else { uriParts[0] = uri; }
        return uriParts;
    }

    private String buildQueryString(String uri, Map<String, String> queries) {
        String[] uriParts = splitSubResource(uri);
        Map<String, String> sortMap = new TreeMap<String, String>(queries);
        if (null != uriParts[1]) {
            sortMap.put(uriParts[1], null);
        }
        StringBuilder queryBuilder = new StringBuilder(uriParts[0]);
        if (0 < sortMap.size()) {
            queryBuilder.append("?");
        }
        for (Map.Entry<String, String> e : sortMap.entrySet()) {
            queryBuilder.append(e.getKey());

            String value = e.getValue();
            if (value == null || value.isEmpty()) {
                queryBuilder.append(QUERY_SEPARATOR);
                continue;
            }
            queryBuilder.append("=").append(e.getValue()).append(QUERY_SEPARATOR);
        }
        String queryString = queryBuilder.toString();
        if (queryString.endsWith(QUERY_SEPARATOR)) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        return queryString;

    }

    protected String buildCanonicalHeaders(Map<String, String> headers, String headerBegin) {
        Map<String, String> sortMap = new TreeMap<String, String>();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            String key = e.getKey().toLowerCase();
            String val = e.getValue();
            if (key.startsWith(headerBegin)) {
                sortMap.put(key, val);
            }
        }
        StringBuilder headerBuilder = new StringBuilder();
        for (Map.Entry<String, String> e : sortMap.entrySet()) {
            headerBuilder.append(e.getKey());
            headerBuilder.append(':').append(e.getValue());
            headerBuilder.append(HEADER_SEPARATOR);
        }
        return headerBuilder.toString();
    }

    public static String replaceOccupiedParameters(String url, Map<String, String> paths) {
        String result = url;
        for (Map.Entry<String, String> entry : paths.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String target = "[" + key + "]";
            result = result.replace(target, value);
        }

        return result;
    }

    @Override
    public String composeStringToSign(MethodType method, String uriPattern, Signer signer,
                                      Map<String, String> queries, Map<String, String> headers,
                                      Map<String, String> paths) {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(HEADER_SEPARATOR);
        if (headers.get("Accept") != null) {
            sb.append(headers.get("Accept"));
        }
        sb.append(HEADER_SEPARATOR);
        if (headers.get("Content-MD5") != null) {
            sb.append(headers.get("Content-MD5"));
        }
        sb.append(HEADER_SEPARATOR);
        if (headers.get("Content-Type") != null) {
            sb.append(headers.get("Content-Type"));
        }
        sb.append(HEADER_SEPARATOR);
        if (headers.get("Date") != null) {
            sb.append(headers.get("Date"));
        }
        sb.append(HEADER_SEPARATOR);
        String uri = replaceOccupiedParameters(uriPattern, paths);
        sb.append(buildCanonicalHeaders(headers, "x-acs-"));
        sb.append(buildQueryString(uri, queries));
        return sb.toString();
    }

    public static ISignatureComposer getComposer() {
        if (null == composer) {
            composer = new RoaSignatureComposer();
        }
        return composer;
    }
}
