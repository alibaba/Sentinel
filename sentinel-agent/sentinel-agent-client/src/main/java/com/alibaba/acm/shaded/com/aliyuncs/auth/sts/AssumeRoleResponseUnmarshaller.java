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
package com.alibaba.acm.shaded.com.aliyuncs.auth.sts;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.AssumeRoleResponse.Credentials;
import com.alibaba.acm.shaded.com.aliyuncs.auth.sts.AssumeRoleResponse.AssumedRoleUser;
import com.alibaba.acm.shaded.com.aliyuncs.transform.UnmarshallerContext;


public class AssumeRoleResponseUnmarshaller {

    public static AssumeRoleResponse unmarshall(AssumeRoleResponse assumeRoleResponse, UnmarshallerContext context) {

        assumeRoleResponse.setRequestId(context.stringValue("AssumeRoleResponse.RequestId"));

        Credentials credentials = new Credentials();
        credentials.setSecurityToken(context.stringValue("AssumeRoleResponse.Credentials.SecurityToken"));
        credentials.setAccessKeySecret(context.stringValue("AssumeRoleResponse.Credentials.AccessKeySecret"));
        credentials.setAccessKeyId(context.stringValue("AssumeRoleResponse.Credentials.AccessKeyId"));
        credentials.setExpiration(context.stringValue("AssumeRoleResponse.Credentials.Expiration"));
        assumeRoleResponse.setCredentials(credentials);

        AssumedRoleUser  assumedRoleUser = new AssumedRoleUser();
        assumedRoleUser.setArn(context.stringValue("AssumeRoleResponse.AssumedRoleUser.Arn"));
        assumedRoleUser.setAssumedRoleId(context.stringValue("AssumeRoleResponse.AssumedRoleUser.AssumedRoleId"));
        assumeRoleResponse.setAssumedRoleUser(assumedRoleUser);

        return assumeRoleResponse;
    }
}