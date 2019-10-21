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

package com.alibaba.acm.shaded.com.aliyuncs.endpoint.location.transform.v20150612;


import java.util.ArrayList;
import java.util.List;

import com.alibaba.acm.shaded.com.aliyuncs.endpoint.location.model.v20150612.DescribeEndpointsResponse;
import com.alibaba.acm.shaded.com.aliyuncs.endpoint.location.model.v20150612.DescribeEndpointsResponse.Endpoint;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;


public class DescribeEndpointsResponseUnmarshaller {

    public static DescribeEndpointsResponse unmarshall(DescribeEndpointsResponse describeEndpointsResponse, UnmarshallerContext context) {

        describeEndpointsResponse.setRequestId(context.stringValue("DescribeEndpointsResponse.RequestId"));
        describeEndpointsResponse.setSuccess(context.booleanValue("DescribeEndpointsResponse.Success"));

        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        for (int i = 0; i < context.lengthValue("DescribeEndpointsResponse.Endpoints.Length"); i++) {
            Endpoint endpoint = new Endpoint();
            endpoint.setEndpoint(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].Endpoint"));
            endpoint.setId(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].Id"));
            endpoint.setNamespace(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].Namespace"));
            endpoint.setSerivceCode(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].SerivceCode"));
            endpoint.setType(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].Type"));

            List<String> protocols = new ArrayList<String>();
            for (int j = 0; j < context.lengthValue("DescribeEndpointsResponse.Endpoints["+ i +"].Protocols.Length"); j++) {
                protocols.add(context.stringValue("DescribeEndpointsResponse.Endpoints["+ i +"].Protocols["+ j +"]"));
            }
            endpoint.setProtocols(protocols);

            endpoints.add(endpoint);
        }
        describeEndpointsResponse.setEndpoints(endpoints);

        return describeEndpointsResponse;
    }
}
