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

package com.alibaba.acm.shaded.com.aliyuncs.endpoint.location.model.v20150612;
import com.alibaba.acm.shaded.com.aliyuncs.RpcAcsRequest;

public class DescribeEndpointsRequest extends RpcAcsRequest<DescribeEndpointsResponse> {

    public DescribeEndpointsRequest() {
        super("Location", "2015-06-12", "DescribeEndpoints");
    }

    private String id;

    private String serviceCode;

    private String type;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
        putQueryParameter("Id", id);
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
        putQueryParameter("ServiceCode", serviceCode);
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
        putQueryParameter("Type", type);
    }

    @Override
    public Class<DescribeEndpointsResponse> getResponseClass() {
        return DescribeEndpointsResponse.class;
    }

}