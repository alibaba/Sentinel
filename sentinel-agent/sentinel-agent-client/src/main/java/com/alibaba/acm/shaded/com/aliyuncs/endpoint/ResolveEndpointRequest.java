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

public class ResolveEndpointRequest {

    public static final String ENDPOINT_TYPE_INNER = "innerAPI";
    public static final String ENDPOINT_TYPE_OPEN = "openAPI";

    public String productCode = null;
    public String regionId = null;
    public String endpointType = null;
    public String locationServiceCode = null;
    public String productCodeLower = null;

    public ResolveEndpointRequest(String regionId, String productCode,
                                  String locationServiceCode, String endpointType) {

        this.regionId = regionId;
        this.productCode = productCode;
        this.productCodeLower = productCode.toLowerCase();

        if (endpointType == null || endpointType.length() == 0) {
            endpointType = ENDPOINT_TYPE_OPEN;
        }

        this.endpointType = endpointType;
        this.locationServiceCode = locationServiceCode;

    }

    public boolean isOpenApiEndpoint() {
        return ENDPOINT_TYPE_OPEN.equals(endpointType);
    }
}
