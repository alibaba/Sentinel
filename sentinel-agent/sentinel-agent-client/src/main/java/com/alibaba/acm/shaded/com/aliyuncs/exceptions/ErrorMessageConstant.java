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

package com.alibaba.acm.shaded.com.aliyuncs.exceptions;

public class ErrorMessageConstant {

    public static final String SDK_ENDPOINT_MANAGEMENT_DOC_HTML =
            "https://www.alibabacloud.com/help/zh/doc-detail/92049.htm";

    public static final String INVALID_REGION_ID =
            "No such region '%s'. Please check your region ID.";
    public static final String ENDPOINT_NO_REGION =
            "No endpoint in the region '%s' for product '%s'. \n" +
                    "You can set an endpoint for your request explicitly.%s\n" +
                    "See " + SDK_ENDPOINT_MANAGEMENT_DOC_HTML + "\n";

            // Or use available regions:
    public static final String ENDPOINT_NO_PRODUCT =
            "No endpoint for product '%s'. \n" +
                    "Please check the product code, or set an endpoint for your request explicitly.\n" +
                    "See " + SDK_ENDPOINT_MANAGEMENT_DOC_HTML + "\n";
}
