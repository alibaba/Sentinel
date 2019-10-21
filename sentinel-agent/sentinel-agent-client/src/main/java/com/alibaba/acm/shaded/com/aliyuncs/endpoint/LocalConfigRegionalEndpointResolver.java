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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocalConfigRegionalEndpointResolver extends EndpointResolverBase {

    protected static final String ENDPOINT_JSON = "endpoints.json";
    private Set<String> validRegionIds = new HashSet<String>();
    private Map<String, String> locationCodeMapping = new HashMap<String, String>();
    private JsonObject regionalEndpointData;

    public LocalConfigRegionalEndpointResolver() {
        JsonObject obj = readLocalConfigAsJsonObject();
        initLocalConfig(obj);
    }

    public LocalConfigRegionalEndpointResolver(String configJsonStr) {
        // For testability
        JsonObject obj = (new JsonParser()).parse(configJsonStr).getAsJsonObject();
        initLocalConfig(obj);
    }

    private void initLocalConfig(JsonObject obj) {
        initRegionalEndpointData(obj);
        initRegionIds(obj);
        initLocationCodeMapping(obj);
    }

    private void initRegionalEndpointData(JsonObject obj) {
        if (!obj.has("regional_endpoints")) {
            return;
        }
        regionalEndpointData = obj.get("regional_endpoints").getAsJsonObject();
        JsonObject regionalEndpoints = obj.get("regional_endpoints").getAsJsonObject();
        for (String normalizedProductCode : regionalEndpoints.keySet()) {
            JsonObject productData = regionalEndpoints.get(normalizedProductCode).getAsJsonObject();
            for (String regionId : productData.keySet()) {
                String endpoint = productData.get(regionId).getAsString();
                putEndpointEntry(makeEndpointKey(normalizedProductCode, regionId), endpoint);
            }
        }
    }

    protected void initRegionIds(JsonObject obj) {
        if (!obj.has("regions")) {
            return;
        }
        JsonArray regions = obj.get("regions").getAsJsonArray();
        for (JsonElement regionData : regions) {
            validRegionIds.add(regionData.getAsString());
        }
    }

    protected void initLocationCodeMapping(JsonObject obj) {
        if (!obj.has("location_code_mapping")) {
            return;
        }
        JsonObject mappingData = obj.get("location_code_mapping").getAsJsonObject();
        for (String productCode : mappingData.keySet()) {
            String locationServiceCode = mappingData.get(productCode).getAsString();
            locationCodeMapping.put(productCode, locationServiceCode);
        }
    }

    protected String getNormalizedProductCode(String productCode) {
        String productCodeLower = productCode.toLowerCase();
        if (locationCodeMapping.containsKey(productCodeLower)) {
            return locationCodeMapping.get(productCodeLower);
        }
        return productCodeLower;
    }

    protected JsonObject readLocalConfigAsJsonObject() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream(ENDPOINT_JSON);
        java.util.Scanner scanner = new java.util.Scanner(is,"UTF-8").useDelimiter("\0");
        String jsonStr = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        JsonObject endpointData = (new JsonParser()).parse(jsonStr).getAsJsonObject();
        return endpointData;
    }

    public String resolve(ResolveEndpointRequest request) {
        if (request.isOpenApiEndpoint()) {
            return fetchEndpointEntry(request);
        } else {
            return null;
        }
    }

    @Override
    public String makeEndpointKey(ResolveEndpointRequest request) {
        return makeEndpointKey(request.productCodeLower, request.regionId);
    }

    public String makeEndpointKey(String productCodeLower, String regionId) {
        return getNormalizedProductCode(productCodeLower) + "." + regionId.toLowerCase();
    }

    @Override
    public boolean isRegionIdValid(String regionId) {
        return validRegionIds.contains(regionId);
    }

    @Override
    public Set<String> getValidRegionIdsByProduct(String productCodeLower) {
        String code = getNormalizedProductCode(productCodeLower);
        if (regionalEndpointData != null && regionalEndpointData.has(code)) {
            Set<String> validRegionIdsByProduct = regionalEndpointData.get(code).getAsJsonObject().keySet();
            return validRegionIdsByProduct;
        }
        return null;
    }

    @Override
    public boolean isProductCodeValid(String productCode) {
        return super.isProductCodeValid(getNormalizedProductCode(productCode));
    }
}
