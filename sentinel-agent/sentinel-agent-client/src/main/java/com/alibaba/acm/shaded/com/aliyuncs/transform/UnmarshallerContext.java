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
package com.alibaba.acm.shaded.com.aliyuncs.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.acm.shaded.com.aliyuncs.http.HttpResponse;
import com.alibaba.acm.shaded.com.aliyuncs.utils.FlattenMapUtil;

public class UnmarshallerContext {
    private int httpStatus;
    private Map<String, String> responseMap;
    private HttpResponse httpResponse;

    public Integer integerValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return null;
        }
        return Integer.valueOf(value);
    }

    public String stringValue(String key) {
        return responseMap.get(key);
    }

    public Long longValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return null;
        }
        return Long.valueOf(responseMap.get(key));
    }

    public Boolean booleanValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return null;
        }
        return Boolean.valueOf(responseMap.get(key));
    }

    public Float floatValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return null;
        }
        return Float.valueOf(responseMap.get(key));
    }

    public Double doubleValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return null;
        }
        return Double.valueOf(responseMap.get(key));
    }

    public int lengthValue(String key) {
        String value = responseMap.get(key);
        if (null == value || 0 == value.length()) {
            return 0;
        }
        return Integer.valueOf(responseMap.get(key));
    }

    public List<Map<Object, Object>> listMapValue(String key) {
        return FlattenMapUtil.toListMap(responseMap, key);
    }

    public Map<Object, Object> mapValue(String key) {
        return FlattenMapUtil.toMap(responseMap, key);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Map<String, String> getResponseMap() {
        return responseMap;
    }

    public void setResponseMap(Map<String, String> responseMap) {
        this.responseMap = responseMap;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    public void setHttpResponse(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

}
