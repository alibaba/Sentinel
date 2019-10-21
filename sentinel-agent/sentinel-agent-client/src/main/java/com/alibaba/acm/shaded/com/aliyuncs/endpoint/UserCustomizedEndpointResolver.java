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

import com.alibaba.acm.shaded.com.aliyuncs.exceptions.ClientException;

import java.util.HashSet;
import java.util.Set;

public class UserCustomizedEndpointResolver extends EndpointResolverBase {

    private Set<String> validRegionIds;
    public UserCustomizedEndpointResolver() {
        validRegionIds = new HashSet<String>();
    }

    public void putEndpointEntry(String regionId, String productCode, String endpoint) {
        putEndpointEntry(makeEndpointKey(productCode, regionId), endpoint);
        validRegionIds.add(regionId);
    }

    @Override
    public String resolve(ResolveEndpointRequest request) {
        return fetchEndpointEntry(request);
    }

    @Override
    public String makeEndpointKey(ResolveEndpointRequest request) {
        return makeEndpointKey(request.productCode, request.regionId);
    }

    public String makeEndpointKey(String productCode, String regionId) {
        return productCode.toLowerCase() + "." + regionId.toLowerCase();
    }

    @Override
    public boolean isRegionIdValid(String regionId) {
        return validRegionIds.contains(regionId);
    }
}
