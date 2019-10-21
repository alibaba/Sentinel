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

package com.alibaba.acm.shaded.com.aliyuncs.endpoint;

//import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LocalConfigGlobalEndpointResolver extends LocalConfigRegionalEndpointResolver {

    public LocalConfigGlobalEndpointResolver() {
        JsonObject obj = readLocalConfigAsJsonObject();
        initLocalConfig(obj);
    }

    public LocalConfigGlobalEndpointResolver(String configJsonStr) {
        // For testability
        JsonObject obj = (new JsonParser()).parse(configJsonStr).getAsJsonObject();
        initLocalConfig(obj);
    }

    protected void initLocalConfig(JsonObject obj) {
        initGlobalEndpointData(obj);
        initRegionIds(obj);
    }

    private void initGlobalEndpointData(JsonObject obj) {
        if (!obj.has("global_endpoints")) {
            return;
        }
        JsonObject globalEndpoints = obj.get("global_endpoints").getAsJsonObject();
        for (String locationServiceCode : globalEndpoints.keySet()) {
            String endpoint = globalEndpoints.get(locationServiceCode).getAsString();
            putEndpointEntry(makeEndpointKey(locationServiceCode), endpoint);
        }
    }

    @Override
    public String resolve(ResolveEndpointRequest request) {
        if (request.isOpenApiEndpoint() && isRegionIdValid(request.regionId)) {
            return fetchEndpointEntry(request);
        } else {
            return null;
        }
    }

    @Override
    public String makeEndpointKey(ResolveEndpointRequest request) {
        return makeEndpointKey(request.productCodeLower);
    }

    public String makeEndpointKey(String productCodeLower) {
        return getNormalizedProductCode(productCodeLower);
    }
}
