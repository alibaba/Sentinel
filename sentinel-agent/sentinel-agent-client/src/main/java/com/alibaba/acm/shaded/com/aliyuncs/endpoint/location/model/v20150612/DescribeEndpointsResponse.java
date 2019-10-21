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

import java.util.List;
import com.alibaba.acm.shaded.com.aliyuncs.AcsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.endpoint.location.transform.v20150612.DescribeEndpointsResponseUnmarshaller;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;

/**
 * @author auto create
 * @version
 */
public class DescribeEndpointsResponse extends AcsResponse {

    private String requestId;

    private Boolean success;

    private List<Endpoint> endpoints;

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<Endpoint> getEndpoints() {
        return this.endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public static class Endpoint {

        private String endpoint;

        private String id;

        private String namespace;

        private String serivceCode;

        private String type;

        private List<String> protocols;

        public String getEndpoint() {
            return this.endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNamespace() {
            return this.namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getSerivceCode() {
            return this.serivceCode;
        }

        public void setSerivceCode(String serivceCode) {
            this.serivceCode = serivceCode;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<String> getProtocols() {
            return this.protocols;
        }

        public void setProtocols(List<String> protocols) {
            this.protocols = protocols;
        }
    }

    @Override
    public DescribeEndpointsResponse getInstance(UnmarshallerContext context) {
        return  DescribeEndpointsResponseUnmarshaller.unmarshall(this, context);
    }
}
